package de.intranda.goobi.plugins.step.datacite.metadata;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class Metadata {
			
	public static String getMetadataFromFile() throws Exception{
		File metadataFile = new File("resource/metadata.xml");
		byte[] fileBytes = readFile(metadataFile);
		return new String(fileBytes);		
	}

	private static byte[] readFile(File inputFile) throws Exception{
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(inputFile));
			
		byte[] buffer = new byte[1024*10];
		
		int length;
		while ((length=bin.read(buffer)) != -1){
			byteStream.write(buffer, 0, length);
		}
		
		bin.close();		
		return byteStream.toByteArray();
	}
	
}
