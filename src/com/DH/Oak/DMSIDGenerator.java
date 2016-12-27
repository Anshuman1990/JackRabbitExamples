package com.DH.Oak;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.UUID;


public class DMSIDGenerator {
	


	private static final String MAC_ADDRESS = getMacAddress();
	
	private static final String HYPHEN = "-";
	private static final String FORMAT_STRING = "%02X%s";
	
	private static String getMacAddress () {
		StringBuffer sb = new StringBuffer("");
		
		try {
			InetAddress address = InetAddress.getLocalHost();
			NetworkInterface nInterface = NetworkInterface.getByInetAddress(address);
			if(nInterface!=null){
				byte[] hardwareAddress = nInterface.getHardwareAddress();
				int length = hardwareAddress.length;
				for (int i = 0; i < length; i++) {
					sb.append(String.format(FORMAT_STRING, hardwareAddress[i], (i < length -1) ? HYPHEN : ""));
				}
			}else{
				Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
				 while(networks.hasMoreElements()) {
				    	NetworkInterface network = networks.nextElement();
				        byte[] mac = network.getHardwareAddress();
				        if(mac != null) {
				        	for (int i = 0; i < mac.length; i++) {
				                sb.append(String.format(FORMAT_STRING, mac[i], (i < mac.length - 1) ? HYPHEN : ""));
				              }
				        }
			    }
			}
		} catch (Exception e) {
		e.printStackTrace();
		}
		
		return sb.toString();
	}

	private DMSIDGenerator() {	}

	public static String createDMSUniqueID() {
		UUID uuid = UUID.randomUUID();
		StringBuffer sb = new StringBuffer();
		sb.append(uuid.toString());
		sb.append(MAC_ADDRESS);
		return sb.toString();
	}
}