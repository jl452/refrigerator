package com.akzia.inets.refrigerator.rest;

import com.akzia.inets.refrigerator.Utils;
import com.akzia.inets.refrigerator.model.Brand;
import com.akzia.inets.refrigerator.model.BrandImage;
import com.akzia.inets.refrigerator.model.Manager;
import com.akzia.inets.refrigerator.model.Merchandiser;
import com.akzia.inets.refrigerator.model.Refrigerator;
import com.akzia.inets.refrigerator.model.RefrigeratorState;
import com.akzia.inets.refrigerator.model.Shop;
import com.akzia.inets.refrigerator.model.ShopStatistics;
import com.akzia.inets.refrigerator.service.RefrigeratorService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Controller
//@Scope(value = "session")
@RequestMapping("/rest")
public class RestServlet /*implements ServletContextAware*/{
	protected static final Logger logger = Utils.createLogger();

	@Autowired
	RefrigeratorService service;

	/*@Override
	public void setServletContext(ServletContext servletContext){
		String username = servletContext.getInitParameter("username");
		String password = servletContext.getInitParameter("password");
	}*/

	public static HashMap<String, String> createDescriptionMap(String[] description){
		HashMap<String, String> descriptionMap = new HashMap<>();
		for (String volumes : description){
			String separator = "MagicalSeparatorString";
			String[] volume = volumes.split(separator);
			if (volume.length != 2){
				throw new IllegalArgumentException("Description-item must contain 2 items with separator = [" + separator + "] :[" + volumes + "]");
			}
			descriptionMap.put(String.valueOf(volume[0]), String.valueOf(volume[1]));
		}
		return descriptionMap;
	}

	@RequestMapping(value = "/addBrandImage")
	@ResponseBody
	public void addBrandImage(@RequestParam String brandName, @RequestParam String brandImageBase64){
		service.addBrandImage(brandName, brandImageBase64, false);
	}

	@RequestMapping(value = "/addBrandLogo")
	@ResponseBody
	public void addBrandLogo(@RequestParam String brandName, @RequestParam String brandImageBase64){
		service.addBrandImage(brandName, brandImageBase64, true);
	}

	@RequestMapping(value = "/updateBrand")
	@ResponseBody
	public long updateBrand(@RequestParam long brandId, @RequestParam String brandName){
		return service.updateBrand(brandId, brandName);
	}

	@RequestMapping(value = "/removeBrandImage")
	@ResponseBody
	public void removeBrandImage(@RequestParam long brandImageId){
		service.removeBrandImage(brandImageId);
	}

	@RequestMapping(value = "/updateManager")
	@ResponseBody
	public long updateManager(@RequestParam long managerId, @RequestParam String managerName, @RequestParam Long[] merchandiserIds){
		return service.updateManager(managerId, managerName, merchandiserIds);
	}

	@RequestMapping(value = "/removeManager")
	@ResponseBody
	public void removeManager(@RequestParam long managerId){
		service.removeManager(managerId);
	}

	@RequestMapping(value = "/updateMerchandiser")
	@ResponseBody
	public long updateMerchandiser(@RequestParam long merchandiserId, @RequestParam String merchandiserName, @RequestParam Long[] shopIds, @RequestParam String[] description){
		HashMap<String, String> descriptionMap = createDescriptionMap(description);
		return service.updateMerchandiser(merchandiserId, merchandiserName, shopIds, descriptionMap);
	}

	@RequestMapping(value = "/removeMerchandiser")
	@ResponseBody
	public void removeMerchandiser(@RequestParam long merchandiserId){
		service.removeMerchandiser(merchandiserId);
	}

	@RequestMapping(value = "/updateShop")
	@ResponseBody
	public long updateShop(@RequestParam long shopId, @RequestParam String shopName, @RequestParam Long[] refrigeratorIds, @RequestParam int oos, @RequestParam String[] description){
		HashMap<String, String> descriptionMap = createDescriptionMap(description);
		return service.updateShop(shopId, shopName, refrigeratorIds, oos, descriptionMap);
	}

	@RequestMapping(value = "/updateRefrigerator")
	@ResponseBody
	public long updateRefrigerator(@RequestParam long refrigeratorId, @RequestParam String refrigeratorName){
		return service.updateRefrigerator(refrigeratorId, refrigeratorName);
	}

	//in GET volumes=tuborg1,2&volumes=tuborg2,3,tuborg1,4
	@RequestMapping(value = "/addState")
	@ResponseBody
	public void addState(@RequestParam long refrigeratorId, @RequestParam String[][] volumes, @RequestParam String creationLocalTime) throws ParseException{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd.hh:mm:ss");
		service.addRefrigeratorState(refrigeratorId, volumes, false, null, df.parse(creationLocalTime));
	}

	@RequestMapping(value = "/addPlanogram")
	@ResponseBody
	public void addPlanogram(@RequestParam long refrigeratorId, @RequestParam String[][] volumes, @RequestParam String planogramName){
		service.addRefrigeratorState(refrigeratorId, volumes, true, planogramName, null);
	}

	@RequestMapping(value = "/getBrands")
	@ResponseBody
	public List<Brand> getBrands(){
		return service.getAllBrands();
	}

	@RequestMapping(value = "/getBrandImages")
	@ResponseBody
	public List<BrandImage> getBrandImages(@RequestParam long brandId){
		return service.getAllBrandImages(brandId, false);
	}

	@RequestMapping(value = "/getBrandLogos")
	@ResponseBody
	public List<BrandImage> getBrandLogos(@RequestParam long brandId){
		return service.getAllBrandImages(brandId, true);
	}

	@RequestMapping(value = "/getImage")
	@ResponseBody
	public ResponseEntity<byte[]> getImage(@RequestParam long imageId){
		final HttpHeaders headers = new HttpHeaders();
		headers.setContentType(new MediaType("image", "image"));
		headers.setContentDispositionFormData("attachment", "image" + imageId + ".img");
		return new ResponseEntity<>(service.getImage(imageId), headers, HttpStatus.CREATED);
	}

	@RequestMapping(value = "/getManagers")
	@ResponseBody
	public List<Manager> getManagers(){
		return service.getAllManagers();
	}

	@RequestMapping(value = "/getMerchandisers")
	@ResponseBody
	public Collection<Merchandiser> getMerchandisers(@RequestParam long managerId){
		return service.getAllMerchandisersByManagerId(managerId);
	}

	@RequestMapping(value = "/getShops")
	@ResponseBody
	public Collection<Shop> getShops(/*@RequestParam*/ Long managerId, Long merchandiserId){
		if (managerId != null){
			return service.getAllShopsByManagerId(managerId);
		} else if (merchandiserId != null){
			return service.getAllShopsByMerchandiserId(merchandiserId);
		} else {
			throw new IllegalArgumentException("Need parameter [managerId] or [merchandiserId]");
		}
	}

	@RequestMapping(value = "/getRefrigerators")
	@ResponseBody
	public Collection<Refrigerator> getRefrigerators(@RequestParam long shopId){
		return service.getAllRefrigeratorsByShopId(shopId);
	}

	@RequestMapping(value = "/getCurrentState")
	@ResponseBody
	public RefrigeratorState getCurrentState(@RequestParam long refrigeratorId){
		return service.getCurrentRefrigeratorState(refrigeratorId, false);
	}

	@RequestMapping(value = "/getCurrentPlanogram")
	@ResponseBody
	public RefrigeratorState getCurrentPlanogram(@RequestParam long refrigeratorId){
		return service.getCurrentRefrigeratorState(refrigeratorId, true);
	}

	//used for device
	@RequestMapping(value = "/getPlan")
	@ResponseBody
	public String getPlan(@RequestParam long refrigeratorId){
		return service.getPlan(refrigeratorId);
	}

	/*@RequestMapping(value = "/getCoordinates")
	@ResponseBody
	public String getCoordinates(@RequestParam long refrigeratorId){
		return service.getCoordinates(refrigeratorId);
	}*/

	@RequestMapping(value = "/getNeedForShopValue")
	@ResponseBody
	public HashMap<Long, Long> getNeedForShopValue(@RequestParam long shopId){
		return service.getNeedForShopValue(shopId);
	}

	@RequestMapping(value = "/getSoldByShopYesterday")
	@ResponseBody
	public HashMap<Long, Long> getSoldByShopYesterday(@RequestParam long shopId){
		return service.getSoldByShopYesterday(shopId);
	}

	@RequestMapping(value = "/getShopStatistics")
	@ResponseBody
	public List<ShopStatistics> getShopStatistics(@RequestParam long shopId){
		return service.getShopStatistics(shopId);
	}


	//TODO REMOVE
	@RequestMapping(value = "/test")
	@ResponseBody
	public Manager test(@RequestParam String p1, @RequestParam String[][] volumes){
		logger.info("p1=[" + p1 + "]");
		for (String[] volume1 : volumes){
			String s = "";
			for (String volume : volume1){
				s += "[" + volume + "]";
			}
			logger.info("\tvolumes=[" + s + "]");
		}

		List<Brand> brands = service.getAllBrands();
		logger.info("brands.size = [" + String.valueOf(brands.size()) + "]");
		for (Brand b : brands){
			Collection<BrandImage> brandImages = service.getAllBrandImages(b.getId(), false);
			logger.info("\t[" + b + "] [" + b.getId() + "][" + b.getName() + "] [" + brandImages.size() + "]");
			for (BrandImage image : brandImages){
				logger.info("\t\t\t[" + image.getId() + "] [" + image.getRecognitionImageData().length + "]");
			}
		}

		service.test();

		return service.test2();
	}

	@RequestMapping(value = "/test2")
	@ResponseBody
	public com.akzia.inets.refrigerator.model.RefrigeratorLine test2(@RequestParam long id, @RequestParam String[][] volumes, @RequestParam String date) throws ParseException{
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
		logger.info("date=" + df.parse(date));
		logger.info("id=[" + id + "]");
		for (String[] volume : volumes){
			logger.info(String.valueOf(volume[0]) + "=" + String.valueOf(volume[1]));
		}
		logger.info("date=" + date);
		return service.test4(id);
	}

	@RequestMapping(value = "/test3")
	@ResponseBody
	public void test3(@RequestParam long shopId, @RequestParam String shopName, @RequestParam Long[] refrigeratorIds, @RequestParam int oos, @RequestParam String[] description){
		HashMap<String, String> descriptionMap = new HashMap<>();
		for (String volumes : description){
			logger.info("volumes:[" + volumes + "]");
/*			for (String volume : volumes){
				logger.info("volume=" + volume);
			}*/
		}
	}
}