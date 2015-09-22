package com.akzia.inets.refrigerator.model;

import org.codehaus.jackson.annotate.JsonIgnore;

import javax.imageio.ImageIO;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

@Entity
@Table(name = "brandImage")
public class BrandImage implements Serializable{
	@Id
	@GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "brandImage_SEQUENCE_GENERATOR")
	@SequenceGenerator(name = "brandImage_SEQUENCE_GENERATOR", sequenceName = "brandImage_sequence")
	private long id;
	private long brandId;
	@JsonIgnore
	private byte[] recognitionImageData;
	/*@Transient
	private BufferedImage recognitionImage;*/
	@JsonIgnore
	private boolean isLogo = false;

	private BrandImage(){
	}

	public BrandImage(long brandId, byte[] image, boolean isLogo){
		this.brandId = brandId;
		this.isLogo = isLogo;
		this.recognitionImageData = image;
	}

	public BrandImage(long brandId, RenderedImage image, boolean isLogo) throws IOException{
		this.brandId = brandId;
		this.isLogo = isLogo;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(image, "png", out);
		out.flush();
		this.recognitionImageData = out.toByteArray();
		out.close();
	}

	public long getId(){
		return id;
	}

	public long getBrandId(){
		return brandId;
	}

	public byte[] getRecognitionImageData(){
		return recognitionImageData;
	}

	/*public BufferedImage getRecognitionImage() throws IOException{
		if (recognitionImage == null && recognitionImageData != null){
			ByteArrayInputStream in = new ByteArrayInputStream(recognitionImageData);
			recognitionImage = ImageIO.read(in);
			in.close();
		}
		return recognitionImage;
	}*/

	public boolean isLogo(){
		return isLogo;
	}
}
