package eu.emi.emir.client.security.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import eu.emi.emir.client.security.ISecurityProperties;
import eu.emi.emir.client.util.Log;



public class KeystoreUtil {
	private final static Logger log=Log.getLogger(Log.EMIR_SECURITY,KeystoreUtil.class);
	private KeystoreUtil(){}
	private final static AtomicInteger counter=new AtomicInteger(0);
	public static String getDefaultKeyAlias(ISecurityProperties sec) throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		KeyStore keystore = createKeyStore(sec.getKeystore(),sec.getKeystorePassword(),sec.getKeystoreType());
		Enumeration<String> e=keystore.aliases();
		while(e.hasMoreElements()){
			String a=e.nextElement();
			if(keystore.isKeyEntry(a))return a;
		}
		return null;
	}
	
	
	public static KeyStore createKeyStore(String name, String passwd, String type) 
      throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
      if (name == null) {
          throw new IllegalArgumentException("Keystore/Truststore name may not be null");
      }
      String storeType=type!=null ? type : "jks";
      KeyStore keystore  = KeyStore.getInstance(storeType);
      InputStream is = null;
      try {
      	is = new FileInputStream(name); 
          keystore.load(is, passwd!= null ? passwd.toCharArray(): null);
      } finally {
      	if (is != null) is.close();
      }
      return keystore;
  }
	
	public static String[] getTrustedCertDNs(KeyStore ks)throws KeyStoreException{
		Enumeration<String>aliases=ks.aliases();
		List<String>result=new ArrayList<String>();
		while(aliases.hasMoreElements()){
			String a=aliases.nextElement();
			if(ks.isCertificateEntry(a)){
				X509Certificate cert=(X509Certificate)ks.getCertificate(a);
				result.add(cert.getSubjectX500Principal().getName());
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	/**
	 * loads keystore from a file 
	 * 
	 * TODO this code can be replaced by the future authn lib
	 * 
	 * @param name - the file name
	 * @param passwd - the password
	 * @param type - the type (jks, pkcs12). If not given, it will be attempted to infer
	 *               the type from the filename. If impossible, jks and p12 will be tried
	 * 
	 * @return keystore
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 */
	public static KeyStore loadKeyStore(String name, String passwd, String type) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException{
		if (name == null) {
			throw new IllegalArgumentException("Keystore/Truststore name may not be null");
		}
		File f=new File(name);
		if(!f.exists())throw new FileNotFoundException("No such file : "+name);

		if(type==null){
			String fName=f.getName().toLowerCase();
			if(fName.endsWith(".jks")){
				type="jks";
			}
			else if(fName.endsWith(".p12") || fName.endsWith(".pkcs12")){
				type="pkcs12";
			}
		}
		String storeType=type!=null ? type : "jks";
		KeyStore keystore  = KeyStore.getInstance(storeType);
		InputStream is = null;
		try {
			is = new FileInputStream(f); 
			keystore.load(is, passwd!= null ? passwd.toCharArray(): null);
		}catch(IOException ex){
			if(type!=null){
				throw ex;
			}
			else{
				 keystore  = KeyStore.getInstance("pkcs12");
				 is = new FileInputStream(f); 
				 keystore.load(is, passwd!= null ? passwd.toCharArray(): null);
			}
		} finally {
			if (is != null) is.close();
		}
		return keystore;
	}

	


	/**
	 * loads a truststore
	 * 
	 * TODO this code can be replaced by the future authn lib
	 * 
	 * @param file - the file name
	 * @param password - the password (if required)
	 * @param type - the optional type (jks, pkcs12, pem, directory)
	 * 
	 * @return
	 * @throws KeyStoreException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyStore loadTruststore(String file,String password, String type)
			throws KeyStoreException,CertificateException,IOException,NoSuchAlgorithmException{
		KeyStore trustStore=null;
		if(type==null){
			File f=new File(file);
			if(!f.exists())throw new FileNotFoundException("No such file : "+file);
			if(f.isDirectory()){
				type="directory";
			}
			else if(f.getName().toLowerCase().endsWith("pem")){
				type="file";
			}
			else type="jks";
		}
		if("file".equalsIgnoreCase(type)){
			return loadTruststoreFromPemFile(file);
		}
		else if(type.startsWith("dir")){
			return loadTruststoreFromDirectory(file);
		}
		else{	
			trustStore=KeyStore.getInstance(type);
			FileInputStream fis=new FileInputStream(file);
			try{
				trustStore.load(new FileInputStream(file), password.toCharArray());
			}finally{
				fis.close();
			}
		}
		return trustStore;
	}

	private static KeyStore loadTruststoreFromPemFile(String name) throws IOException,NoSuchAlgorithmException,CertificateException,KeyStoreException {
		KeyStore trustStore= KeyStore.getInstance("jks");
		trustStore.load(null, "unicore".toCharArray());
		loadPemFile(name,trustStore);
		return trustStore;
	}

	//load all the certs from the given pem file and put them as trusted certs into the keystore
	private static void loadPemFile(String name, KeyStore ks)throws IOException, KeyStoreException, CertificateException{
		CertificateFactory cf=CertificateFactory.getInstance("X.509");
		BufferedInputStream bis=new BufferedInputStream(new FileInputStream(name));
		try{
			while(true){
				X509Certificate cert=(X509Certificate)cf.generateCertificate(bis);
				String dn=cert.getSubjectX500Principal().toString();
				ks.setCertificateEntry("trusted-"+counter.incrementAndGet(), cert);
				log.debug("Added trusted certificate: "+dn+" loaded from file <"+name+">");
				//check if we have more data...
				bis.mark(1);
				if(bis.read()==-1)break;
				bis.reset();
			}
		}
		finally{
			try{bis.close();}catch(IOException io){};
		}
	}

	private static KeyStore loadTruststoreFromDirectory(String name) throws IOException,NoSuchAlgorithmException,CertificateException,KeyStoreException {
		KeyStore trustStore= KeyStore.getInstance("jks");
		trustStore.load(null, "unicore".toCharArray());
		File directory=new File(name);
		File[] pems=directory.listFiles(new FilenameFilter(){
			public boolean accept(File f, String name){
				return name.endsWith(".pem") || name.endsWith(".cert");
			}
		});
		for(File pem: pems){
			loadPemFile(pem.getAbsolutePath(),trustStore);
		}
		return trustStore;
	}
}
