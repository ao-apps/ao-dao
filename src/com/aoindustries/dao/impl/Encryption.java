/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2013, 2015, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-dao.
 *
 * ao-dao is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-dao is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-dao.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.dao.impl;

import com.aoindustries.security.HashedKey;
import com.aoindustries.security.HashedPassword;
import com.aoindustries.util.WrappedException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Provides encryption routines.
 */
public class Encryption {

    private Encryption() {
    }

    private static String hexEncode(byte[] bytes) {
        int len = bytes.length;
        StringBuilder sb = new StringBuilder(len*2);
        for(int c=0; c<len; c++) {
            int b = bytes[c];
            sb.append(hexChars[(b>>4)&0xf]);
            sb.append(hexChars[b&0xf]);
        }
        return sb.toString();
    }

    /**
     * Performs a one-way hash of the plaintext value using SHA-1.
     *
     * @exception  WrappedException  if any problem occurs.
	 * 
	 * @deprecated  Use salted algorithm, update database of stored passwords as passwords are validated
	 * 
	 * @see  HashedPassword for proper password hashing
	 * @see  HashedKey for SHA-256 hashing
     */
    public static String hash(String plaintext) throws WrappedException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update(plaintext.getBytes("UTF-8"));
            return hexEncode(md.digest());
        } catch(NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new WrappedException(e);
        }
    }

    private static final Random random = new SecureRandom();

    /**
     * Gets the secure random.
     */
    private static Random getRandom() {
        return random;
    }

    private static final char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Generates a random key.
     */
    public static String generateKey() {
        byte[] bytes = new byte[32];
        getRandom().nextBytes(bytes);
        char[] chars = new char[64];
        for(int c=0;c<32;c++) {
            byte b = bytes[c];
            chars[c*2]=hexChars[(b&255)>>>4];
            chars[c*2+1]=hexChars[b&15];
        }
        return new String(chars);
    }

    /*
    public static void main(String[] args) {
        //args = new String[] {"test"};
        if(args.length==0) {
            System.err.println("usage: "+Encryption.class.getName()+" plaintext ...");
            System.exit(1);
        } else {
            for(String arg : args) {
                System.out.println(arg+'\t'+hash(arg));
            }
        }
    }
     */
}
