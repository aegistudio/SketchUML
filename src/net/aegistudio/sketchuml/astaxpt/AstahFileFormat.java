package net.aegistudio.sketchuml.astaxpt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import JP.co.esm.caddies.golf.model.EntityRoot;
import JP.co.esm.caddies.jomt.jmodel.ModelManageInfo;

/**
 * Represents a basic Astah file format. It is a GZIP file with a
 * single entry EntityStore, and the single entry is made up of a
 * ModelManageInfo and an EntityRoot.
 * 
 * This class implements the serialization methods of such format.
 * 
 * @author Haoran Luo
 */
public class AstahFileFormat {
	/**
	 * The magic number that is padded before EntityStore file in the
	 * ZIP in which astah persistent its format as.
	 */
	public static final byte[] NOISE_PADDINGS = {0, (byte)-18, 0, 1};
	
	/**
	 * The model manage information that indicates the version and
	 * software compatibility of the astah format.
	 */
	public ModelManageInfo mmi;
	
	/**
	 * Persist the astah project structure and entities in order.
	 */
	public EntityRoot root;
	
	public void read(File targetFile) throws 
			ZipException, IOException, ClassNotFoundException {
		
		try(	ZipFile zipFile = new ZipFile(targetFile);
				InputStream fis = zipFile.getInputStream(
						zipFile.getEntry("EntityStore"));
				ObjectInputStream ois = new ObjectInputStream(fis);){
			
			// Read the noise paddings and en-validate.
			byte[] noisePaddings = new byte[4];
			fis.read(noisePaddings);
			for(int i = 0; i < noisePaddings.length; ++ i)
				if(NOISE_PADDINGS[i] != noisePaddings[i])
					throw new IOException("Magic number mismatches!");
			
			// Read the model management info.
			Object mmiObject = ois.readObject();
			if(!(mmiObject instanceof ModelManageInfo)) throw 
				new IOException("An object of ModelMangemeInfo is expected!");
			mmi = (ModelManageInfo) mmiObject;
			
			// Read the entity root.
			Object rootObject = ois.readObject();
			if(!(rootObject instanceof EntityRoot)) throw 
				new IOException("An object of EntityRoot is expected!");
			root = (EntityRoot) rootObject;
			
			// Read the null value.
			ois.readObject();
			
			// Finish reading.
			ois.close();
		}
	}
	
	public void write(File targetFile) throws IOException {
		if(mmi == null || root == null) throw new IOException(
				"Neither ModelManageInfo nor EntityRoot should be null while writing");
		try(	FileOutputStream fileOutput = new FileOutputStream(targetFile);
				ZipOutputStream zipOutput = new ZipOutputStream(fileOutput)) {
			
			// Begin writing of entry.
			ZipEntry zipEntry = new ZipEntry("EntityStore");
			zipOutput.putNextEntry(zipEntry);
			ObjectOutputStream oos = new ObjectOutputStream(zipOutput);
			
			// Write out the noise padding.
			zipOutput.write(NOISE_PADDINGS, 0, NOISE_PADDINGS.length);
			
			// Write out the model management and root.
			oos.writeObject(mmi);
			oos.writeObject(root);
			
			// End writing of entry.
			oos.flush();
			oos.reset();
			oos.writeObject(null);
			zipOutput.closeEntry();
		}
	}
}
