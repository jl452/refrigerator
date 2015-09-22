package com.akzia.inets.refrigerator.service;

import com.akzia.inets.refrigerator.Utils;
import com.akzia.inets.refrigerator.dao.RefrigeratorDao;
import com.akzia.inets.refrigerator.model.Brand;
import com.akzia.inets.refrigerator.model.BrandImage;
import com.akzia.inets.refrigerator.model.Manager;
import com.akzia.inets.refrigerator.model.Merchandiser;
import com.akzia.inets.refrigerator.model.OOSEvent;
import com.akzia.inets.refrigerator.model.Refrigerator;
import com.akzia.inets.refrigerator.model.RefrigeratorLine;
import com.akzia.inets.refrigerator.model.RefrigeratorShelf;
import com.akzia.inets.refrigerator.model.RefrigeratorState;
import com.akzia.inets.refrigerator.model.Shop;
import com.akzia.inets.refrigerator.model.ShopStatistics;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
@Transactional(propagation = /*Propagation.SUPPORTS*/ Propagation.REQUIRED, readOnly = true)
//bug http://stackoverflow.com/questions/10181807/spring-3-1-hibernate-4-1-propagation-supports-issue
public class RefrigeratorService{
	protected static final Logger logger = Utils.createLogger();

	@Autowired
	protected RefrigeratorDao dao;

	//TODO orphaned

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void addBrandImage(String brandName, String brandImageBase64, boolean isLogo){
		byte[] binaryData = DatatypeConverter.parseBase64Binary(brandImageBase64);
		/*ByteArrayInputStream in = new ByteArrayInputStream(binaryData);
		BufferedImage image = ImageIO.read(in);
		in.close();
		if (image == null){
			throw new IllegalArgumentException();
		} else {*/
		Brand brand = dao.getFirstBy(Brand.class, "name", brandName);
		if (brand == null){
			brand = new Brand(brandName);
			dao.saveOrUpdate(brand);
		}
		BrandImage brandImage = new BrandImage(brand.getId(), binaryData, isLogo);
		dao.saveOrUpdate(brandImage);
		/*}*/
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateBrand(long itemId, String name){
		Brand item;
		if (itemId != 0){
			item = dao.getById(Brand.class, itemId);
			if (item != null){
				item.setName(name);
			} else {
				throw new IllegalArgumentException("Brand not found with id=[" + itemId + "]");
			}
		} else {
			item = new Brand(name);
		}
		dao.saveOrUpdate(item);
		return item.getId();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateManager(long itemId, String name, Long[] ids){
		List<Merchandiser> list = dao.getAllByIn(Merchandiser.class, "id", ids);
		return updateManager(itemId, name, list);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateManager(long itemId, String name, Collection<Merchandiser> collection){
		Manager item;
		if (itemId != 0){
			item = dao.getById(Manager.class, itemId);
			if (item != null){
				item.setName(name);
				item.setMerchandisers(collection);
			} else {
				throw new IllegalArgumentException("Manager not found with id=[" + itemId + "]");
			}
		} else {
			item = new Manager(name, collection);
		}
		dao.saveOrUpdate(item);
		return item.getId();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateMerchandiser(long itemId, String name, Long[] ids, HashMap<String, String> description){
		List<Shop> list = dao.getAllByIn(Shop.class, "id", ids);
		return updateMerchandiser(itemId, name, list, description);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateMerchandiser(long itemId, String name, Collection<Shop> collection, HashMap<String, String> description){
		Merchandiser item;
		if (itemId != 0){
			item = dao.getById(Merchandiser.class, itemId);
			if (item != null){
				item.setName(name);
				item.setShops(collection);
				item.updateDescription(description);
			} else {
				throw new IllegalArgumentException("Merchandiser not found with id=[" + itemId + "]");
			}
		} else {
			item = new Merchandiser(name, collection, description);
		}
		dao.saveOrUpdate(item);
		return item.getId();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateShop(long itemId, String name, Long[] ids, int oos, HashMap<String, String> description){
		List<Refrigerator> list = dao.getAllByIn(Refrigerator.class, "id", ids);
		return updateShop(itemId, name, list, oos, description);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateShop(long itemId, String name, Collection<Refrigerator> collection, int oos, HashMap<String, String> description){
		Shop item;
		if (itemId != 0){
			item = dao.getById(Shop.class, itemId);
			if (item != null){
				item.setName(name);
				item.setRefrigerators(collection);
				item.setOos(oos);
				item.updateDescription(description);
				updateShopInner(item);
			} else {
				throw new IllegalArgumentException("Shop not found with id=[" + itemId + "]");
			}
		} else {
			item = new Shop(name, collection, oos, description);
		}
		dao.saveOrUpdate(item);
		return item.getId();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	protected void updateShopInner(Shop shop){
		Collection<OOSEvent> oldOosEvents = shop.getOosEvents();
		Collection<OOSEvent> oosEvents = new ArrayList<>();
		int count = 0;
		int countMax = 0;
		for (Refrigerator refrigerator : shop.getRefrigerators()){
			count += refrigerator.getCurrentItems();
			countMax += refrigerator.getCurrentMaxItems();
			if (refrigerator.getCurrentMaxItems() != 0){
				int oosPercent = refrigerator.getCurrentItems() * 100 / refrigerator.getCurrentMaxItems();
				if (oosPercent <= shop.getOos()){
					OOSEvent oosEvent = null;
					for (OOSEvent event : oldOosEvents){
						if (event.getRefrigeratorId() == refrigerator.getId()){
							oosEvent = event;
							break;
						}
					}
					if (oosEvent == null){
						oosEvent = new OOSEvent(refrigerator.getId());
					}
					oosEvents.add(oosEvent);
				}
			}
		}
		shop.setCurrentItems(count);
		shop.setCurrentMaxItems(countMax);
		shop.setOosEvents(oosEvents);
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public long updateRefrigerator(long itemId, String name){
		Refrigerator item;
		if (itemId != 0){
			item = dao.getById(Refrigerator.class, itemId);
			if (item != null){
				item.setName(name);
			} else {
				throw new IllegalArgumentException("Refrigerator not found with id=[" + itemId + "]");
			}
		} else {
			item = new Refrigerator(name);
		}
		dao.saveOrUpdate(item);
		return item.getId();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void removeBrandImage(long itemId){
		Brand item = dao.getById(Brand.class, itemId);
		if (item != null){
			dao.delete(item);
		} else {
			throw new IllegalArgumentException("Brand not found with id=[" + itemId + "]");
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void removeManager(long itemId){
		Manager item = dao.getById(Manager.class, itemId);
		if (item != null){
			dao.delete(item);
		} else {
			throw new IllegalArgumentException("Manager not found with id=[" + itemId + "]");
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void removeMerchandiser(long itemId){
		Merchandiser item = dao.getById(Merchandiser.class, itemId);
		if (item != null){
			//TODO delete from manager
			dao.delete(item);
		} else {
			throw new IllegalArgumentException("Manager not found with id=[" + itemId + "]");
		}
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void addRefrigeratorState(long refrigeratorId, String[][] values, boolean isPlanogram, String name, Date creationLocalTime){
		Refrigerator refrigerator = dao.getById(Refrigerator.class, refrigeratorId);
		if (refrigerator != null){
			RefrigeratorState planogram = null;
			Iterator<RefrigeratorShelf> planogramShelfIterator = null;
			/*RefrigeratorShelf[] oldShelves = null;
			Map<Long, Long> soldMap = null;*/
			if (!isPlanogram){
				planogram = dao.getRefrigeratorStateCurrent(refrigeratorId, true);
				if (planogram == null){
					throw new IllegalArgumentException("Planogram not found for refrigerator with id=[" + refrigeratorId + "]");
				}
				Collection<RefrigeratorShelf> planogramShelves = planogram.getShelves();
				planogramShelfIterator = planogramShelves.iterator();
				int planogramShelvesCount = planogramShelves.size();
				if (values.length != planogramShelvesCount){
					throw new IllegalArgumentException("Shelves count != planogram's shelves count! [" + values.length + "]!=[" + planogramShelvesCount + "]");
				}

				/*RefrigeratorState oldState = dao.getRefrigeratorStateCurrent(refrigeratorId, false);
				if (oldState == null || planogram.getCreationTime().after(oldState.getCreationTime())){
					oldState = planogram;
				}
				Collection<RefrigeratorShelf> t1 = oldState.getShelves();
				oldShelves = t1.toArray(new RefrigeratorShelf[t1.size()]);
				soldMap = refrigerator.getSold();*/
			}

			Collection<RefrigeratorShelf> shelves = new ArrayList<>();
			for (int i1 = 0; i1 < values.length; i1++){
				Iterator<RefrigeratorLine> planogramLinesIterator = null;
				if (!isPlanogram){
					Collection<RefrigeratorLine> planogramLines = planogramShelfIterator.next().getLines();
					int planogramLinesCount = planogramLines.size() * 2;
					if (values[i1].length != planogramLinesCount){
						throw new IllegalArgumentException("Liness count != planogram's lines count! [" + ((double)values[i1].length) / 2 + "]!=[" + planogramLinesCount + "]");
					}
					planogramLinesIterator = planogramLines.iterator();
				}

				Collection<RefrigeratorLine> refrigeratorLines = new ArrayList<>();
				for (int i2 = 0; i2 < values[i1].length; i2 += 2){
					if (values[i1][i2] != null && !values[i1][i2].isEmpty()){
						int intValue = Integer.parseInt(values[i1][i2 + 1]);
						if (intValue > 0){
							Brand brand = null;
							try{
								brand = dao.getById(Brand.class, Integer.parseInt(values[i1][i2]));
							} catch (NumberFormatException ignored){
							}
							if (brand == null){
								brand = dao.getFirstBy(Brand.class, "name", values[i1][i2]);
							}
							if (brand == null){
								throw new IllegalArgumentException("Brand not found for name=[" + values[i1][i2] + "]");
							}

							int filledVolumeMax = isPlanogram ? intValue : planogramLinesIterator.next().getFilledVolume();
							refrigeratorLines.add(new RefrigeratorLine(i2 / 2, brand, intValue, filledVolumeMax));
						} else {
							throw new IllegalArgumentException("Value must be >0 [" + i1 + "][" + i2 + "] intValue=[" + values[i1][i2 + 1] + "]");
						}
					} else {
						throw new IllegalArgumentException("Value must be >0 [" + i1 + "][" + i2 + "] name=[" + String.valueOf(values[i1][i2]) + "]");
					}
				}

				shelves.add(new RefrigeratorShelf(i1, refrigeratorLines));
			}
			RefrigeratorState state = isPlanogram ? new RefrigeratorState(refrigeratorId, shelves, name) : new RefrigeratorState(refrigeratorId, shelves, planogram, creationLocalTime);
			RefrigeratorState oldState = null;
			if (!isPlanogram){
				oldState = dao.getRefrigeratorStateCurrent(refrigeratorId, false);
			}
			dao.saveOrUpdate(state);

			if (isPlanogram){
				refrigerator.setCurrentMaxItems(state.getItemCount());
			} else {
				/*refrigerator.updateSold(soldMap);*/
				if (oldState == null || state.getPlanogram().getCreationTime().after(oldState.getCreationTime())){
					oldState = state.getPlanogram();
				}
				refrigerator.updateSold(soldBetweenState(refrigerator.getSold(), oldState, state));
				refrigerator.setCurrentItems(state.getItemCount());
			}
			dao.saveOrUpdate(refrigerator);

			Shop shop = dao.getShopByRefrigeratorId(refrigeratorId);
			if (shop == null){
				throw new IllegalArgumentException("Shop not found for refrigerator with id=[" + refrigeratorId + "]");
			}
			updateShopInner(shop);
			dao.saveOrUpdate(shop);
		} else {
			throw new IllegalArgumentException("Refrigerator not found with id=[" + refrigeratorId + "]");
		}
	}

	public List<Brand> getAllBrands(){
		return dao.getAll(Brand.class);
	}

	public List<BrandImage> getAllBrandImages(long brandId, boolean isLogo){
		return dao.getAllBy(BrandImage.class, "brandId", brandId, "isLogo", isLogo);
	}

	public byte[] getImage(long imageId){
		BrandImage image = dao.getById(BrandImage.class, imageId);
		if (image == null){
			throw new IllegalArgumentException("Image not found with id=[" + imageId + "]");
		}
		return image.getRecognitionImageData();
	}

	public List<Manager> getAllManagers(){
		return dao.getAll(Manager.class);
	}

	public Collection<Merchandiser> getAllMerchandisersByManagerId(long managerId){
		Manager manager = dao.getById(Manager.class, managerId);
		if (manager == null){
			throw new IllegalArgumentException("Manager not found with id=[" + managerId + "]");
		}
		Collection<Merchandiser> merchandisers = manager.getMerchandisers();
		merchandisers.size();//need for bug: multiple fetch=FetchType.EAGER
		return merchandisers;
	}

	public Collection<Shop> getAllShopsByManagerId(long managerId){
		Manager manager = dao.getById(Manager.class, managerId);
		if (manager == null){
			throw new IllegalArgumentException("Manager not found with id=[" + managerId + "]");
		}
		Collection<Shop> shops = new LinkedList<>();
		for (Merchandiser merchandiser : manager.getMerchandisers()){
			Collection<Shop> shopCollection = merchandiser.getShops();
			for (Shop shop : shopCollection){
				shop.updateRestItems();
				for (OOSEvent oosEvent : shop.getOosEvents()){
					Refrigerator refrigerator = dao.getById(Refrigerator.class, oosEvent.getRefrigeratorId());
					oosEvent.updateRestItems(refrigerator.getName(), refrigerator.getCurrentMaxItems() != 0 ? refrigerator.getCurrentItems() * 100 / refrigerator.getCurrentMaxItems() : 0);
				}
			}
			shops.addAll(shopCollection);
		}
//		shops.size();//need for bug: multiple fetch=FetchType.EAGER
		return shops;
	}

	public Collection<Shop> getAllShopsByMerchandiserId(long merchandiserId){
		Merchandiser merchandiser = dao.getById(Merchandiser.class, merchandiserId);
		if (merchandiser == null){
			throw new IllegalArgumentException("Merchandiser not found with id=[" + merchandiserId + "]");
		}
		Collection<Shop> shops = merchandiser.getShops();
		for (Shop shop : shops){
			shop.updateRestItems();
			for (OOSEvent oosEvent : shop.getOosEvents()){
				Refrigerator refrigerator = dao.getById(Refrigerator.class, oosEvent.getRefrigeratorId());
				oosEvent.updateRestItems(refrigerator.getName(), refrigerator.getCurrentMaxItems() != 0 ? refrigerator.getCurrentItems() * 100 / refrigerator.getCurrentMaxItems() : 0);
			}
		}
//		shops.size();//need for bug: multiple fetch=FetchType.EAGER
		return shops;
	}

	public Collection<Refrigerator> getAllRefrigeratorsByShopId(long shopId){
		Shop shop = dao.getById(Shop.class, shopId);
		if (shop == null){
			throw new IllegalArgumentException("Shop not found with id=[" + shopId + "]");
		}
		Collection<Refrigerator> refrigerators = shop.getRefrigerators();
		refrigerators.size();//need for bug: multiple fetch=FetchType.EAGER
		return refrigerators;
	}

	public RefrigeratorState getCurrentRefrigeratorState(long refrigeratorId, boolean isPlanogram){
		RefrigeratorState state = dao.getRefrigeratorStateCurrent(refrigeratorId, isPlanogram);
		if (state == null){
			throw new IllegalArgumentException((isPlanogram ? "Planogram" : "State") + " not found for refrigerator with id=[" + refrigeratorId + "]");
		}
		for (RefrigeratorShelf shelf : state.getShelves()){
			for (RefrigeratorLine line : shelf.getLines()){
				line.updateRestItems(dao.getFirstBy(BrandImage.class, "brandId", line.getBrand().getId(), "isLogo", true).getId());
				//need for bug: multiple fetch=FetchType.EAGER
				// org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
				// http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
				// multiple fetch=FetchType.EAGER
			}
		}
		return state;
	}

	//used for device
	public String getPlan(long refrigeratorId){
		RefrigeratorState state = dao.getRefrigeratorStateCurrent(refrigeratorId, true);
		if (state == null){
			throw new IllegalArgumentException("Planogram not found for refrigerator with id=[" + refrigeratorId + "]");
		}
		String plan = "creationTime=" + state.getCreationTime().getTime() + "\n";
		for (RefrigeratorShelf shelf : state.getShelves()){
			for (RefrigeratorLine line : shelf.getLines()){
				plan += "brandId=" + line.getBrand().getId() + " x=" + line.getX() + " y=" + line.getY() + ";";
			}
			plan += "\n";
		}
		return plan;
	}

	public HashMap<Long, Long> getNeedForShopValue(long shopId){
		HashMap<Long, Long> needMap = new HashMap<>();
		for (Refrigerator refrigerator : getAllRefrigeratorsByShopId(shopId)){
			summarizeBrandValues(needMap, true, dao.getRefrigeratorStateCurrent(refrigerator.getId(), true));
			summarizeBrandValues(needMap, false, dao.getRefrigeratorStateCurrent(refrigerator.getId(), false));
		}
		return needMap;
	}

	public HashMap<Long, Long> getSoldByShopYesterday(long shopId){
		Calendar now = GregorianCalendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 3);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		Calendar yesterday = (Calendar)now.clone();
		yesterday.set(Calendar.DATE, yesterday.get(Calendar.DATE) - 1);
		HashMap<Long, Long> soldMap = new HashMap<>();

		for (Refrigerator refrigerator : getAllRefrigeratorsByShopId(shopId)){
			List<RefrigeratorState> states = dao.getRefrigeratorStateByDate(refrigerator.getId(), false, yesterday.getTime(), now.getTime());
			if (states.size() > 0){
				Iterator<RefrigeratorState> iterator = states.iterator();
				RefrigeratorState state = iterator.next();
				RefrigeratorState oldState;
//				if (states.size() == 1){
				oldState = dao.getRefrigeratorStateFirstBefore(refrigerator.getId(), false, yesterday.getTime());
				if (oldState == null){
					oldState = state.getPlanogram();
				}
//				} else {
//					oldState = state;
//					state = iterator.next();
//				}
				while (true){
					if (state.getPlanogram().getCreationTime().after(oldState.getCreationTime())){
						oldState = state.getPlanogram();
					}
					soldMap = soldBetweenState(soldMap, oldState, state);
					if (iterator.hasNext()){
						oldState = state;
						state = iterator.next();
					} else {
						break;
					}
				}
			}
		}
		return soldMap;
	}

	public List<ShopStatistics> getShopStatistics(long shopId){
		HashMap<Long, Long> needMap = getNeedForShopValue(shopId);
		HashMap<Long, Long> soldMap = getSoldByShopYesterday(shopId);

		Set<Long> brandIds = new HashSet<>();
		brandIds.addAll(needMap.keySet());
		brandIds.addAll(soldMap.keySet());

		ArrayList<ShopStatistics> statistics = new ArrayList<>();
		for (Long id : brandIds){
			Long need = needMap.get(id);
			Long sold = soldMap.get(id);
			Brand brand = dao.getById(Brand.class, id);
			ShopStatistics shopStatistics = new ShopStatistics(brand.getName(), need != null ? need : 0, sold != null ? sold : 0);
			statistics.add(shopStatistics);
		}
		return statistics;
	}

	protected HashMap<Long, Long> soldBetweenState(HashMap<Long, Long> soldMap, RefrigeratorState oldState, RefrigeratorState state){
		Collection<RefrigeratorShelf> t1 = oldState.getShelves();
		RefrigeratorShelf[] shelvesOld = t1.toArray(new RefrigeratorShelf[t1.size()]);
		t1 = state.getShelves();
		RefrigeratorShelf[] shelves = t1.toArray(new RefrigeratorShelf[t1.size()]);

		//noinspection unchecked
		HashMap<Long, Long> soldMapOld = (HashMap<Long, Long>)soldMap.clone();

		for (int i1 = 0; i1 < shelves.length; i1++){
			Collection<RefrigeratorLine> t2 = shelvesOld[i1].getLines();
			RefrigeratorLine[] linesOld = t2.toArray(new RefrigeratorLine[t2.size()]);
			t2 = shelves[i1].getLines();
			RefrigeratorLine[] lines = t2.toArray(new RefrigeratorLine[t2.size()]);

			for (int i2 = 0; i2 < lines.length; i2++){
				long brandId = lines[i2].getBrand().getId();
				if (brandId != linesOld[i2].getBrand().getId()){//обнаружили несовпадение по линиям - считаем продажи заново по сумарному количеству в холодильнике
					summarizeBrandValues(soldMapOld, true, oldState);
					summarizeBrandValues(soldMapOld, false, state);
					return soldMapOld;
				}
				Long value = soldMap.get(brandId);
				if (value == null){
					value = 0l;
				}
				long sold = (linesOld[i2].getFilledVolume() - lines[i2].getFilledVolume());
				if (sold > 0){
					soldMap.put(brandId, value + sold);
				} else {
					if (sold < 0 && !oldState.isPlanogram()){//обнаружили что в линии стало больше чем было - считаем что выставили до планограмы и считаем продажи заново
						// если это уже планограма - неизвестно что делать
						oldState = state.getPlanogram();
						return soldBetweenState(soldMapOld, oldState, state);
					}
				}
			}
		}
		return soldMap;
	}

	protected void summarizeBrandValues(HashMap<Long, Long> brandValuesMap, boolean isOld, RefrigeratorState state){
		if (state != null){
			for (RefrigeratorShelf shelf : state.getShelves()){
				for (RefrigeratorLine line : shelf.getLines()){
					Brand brand = line.getBrand();
					Long value = brandValuesMap.get(brand.getId());
					if (value == null){
						value = 0l;
					}
					brandValuesMap.put(brand.getId(), value + (isOld ? line.getFilledVolume() : -line.getFilledVolume()));
				}
			}
		}
	}


	//TODO REMOVE
	@Transactional(propagation = Propagation.REQUIRED, readOnly = false)
	public void saveTest(Serializable object){
		dao.saveOrUpdate(object);
	}

	public void test(){
		logger.info("test");
		for (Manager manager : dao.getAll(Manager.class)){
			logger.info("manager id=[" + manager.getId() + "] name=[" + manager.getName() + "]");
			for (Merchandiser merchandiser : manager.getMerchandisers()){
				logger.info("\tmerchandiser id=[" + merchandiser.getId() + "] name=[" + merchandiser.getName() + "]");
				for (Shop shop : merchandiser.getShops()){

					logger.info("\t\tshop id=[" + shop.getId() + "]");
					for (Refrigerator refrigerator : shop.getRefrigerators()){
						logger.info("\t\t\trefrigerator id=[" + refrigerator.getId() + "] soldSize=[" + refrigerator.getSold().keySet().size() + "]");
						for (Long brandId : refrigerator.getSold().keySet()){
							Brand brand = dao.getById(Brand.class, brandId);
							logger.info("\t\t\t\tbrand id=[" + brand.getId() + "] name=[" + brand.getName() + "] sold=[" + refrigerator.getSold().get(brandId) + "]");
						}

						RefrigeratorState state = dao.getRefrigeratorStateCurrent(refrigerator.getId(), true);
						if (state != null){
							logger.info("\t\t\t\tplanogram id=[" + state.getId() + "] size=[" + state.getShelves().size() + "]");
							for (RefrigeratorShelf shelf : state.getShelves()){
								logger.info("\t\t\t\t\tshelf id=[" + shelf.getId() + "] size=[" + shelf.getLines().size() + "]");
								for (RefrigeratorLine line : shelf.getLines()){
									logger.info("\t\t\t\t\t\tline id=[" + line.getId() + "] brandId=[" + line.getBrand().getId() + "] name=[" + line.getBrand().getName() + "] value=[" + line.getFilledVolume() + "]");
								}
							}
						}
						state = dao.getRefrigeratorStateCurrent(refrigerator.getId(), false);
						if (state != null){
							logger.info("\t\t\t\tstate id=[" + state.getId() + "] size=[" + state.getShelves().size() + "]");
							for (RefrigeratorShelf shelf : state.getShelves()){
								logger.info("\t\t\t\t\tshelf id=[" + shelf.getId() + "] size=[" + shelf.getLines().size() + "]");
								for (RefrigeratorLine line : shelf.getLines()){
									logger.info("\t\t\t\t\t\tline id=[" + line.getId() + "] brandId=[" + line.getBrand().getId() + "] name=[" + line.getBrand().getName() + "] value=[" + line.getFilledVolume() + "]");
								}
							}
						}
					}
				}
			}
		}
	}

	public Manager test2(){
		logger.info("test2");
		Manager manager = dao.getAll(Manager.class).iterator().next();
		logger.info("manager id=[" + manager.getId() + "] name=[" + manager.getName() + "]");
		for (Merchandiser merchandiser : manager.getMerchandisers()){
			logger.info("\tmerchandiser id=[" + merchandiser.getId() + "] name=[" + merchandiser.getName() + "]");
			for (Shop shop : merchandiser.getShops()){
				logger.info("\t\tshop id=[" + shop.getId() + "] name=[" + shop.getName() + "]");
				for (Refrigerator refrigerator : shop.getRefrigerators()){
					logger.info("\t\t\trefrigerator id=[" + refrigerator.getId() + "] soldSize=[" + refrigerator.getSold().keySet().size() + "]");
				}
			}
		}
		return manager;
	}

	public RefrigeratorShelf test3(long id){
		RefrigeratorShelf shelf = dao.getById(RefrigeratorShelf.class, id);
		for (RefrigeratorLine line : shelf.getLines()){
			line.getBrand().getId();//need for bug
			line.getBrand().getName();//need for bug
			// org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
			// http://stackoverflow.com/questions/4334970/hibernate-cannot-simultaneously-fetch-multiple-bags
			// multiple fetch=FetchType.EAGER
		}
		return shelf;
	}

	public RefrigeratorLine test4(long id){
		RefrigeratorLine line = dao.getById(RefrigeratorLine.class, id);
		return line;
	}

	public Brand test5(long id){
		Brand brand = dao.getById(Brand.class, id);
		return brand;
	}
}