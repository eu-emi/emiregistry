package eu.emi.client.security.utl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import eu.emi.client.security.ISecurityProperties;



public class KeystoreUtil {

	private KeystoreUtil(){}
	
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
	
}
