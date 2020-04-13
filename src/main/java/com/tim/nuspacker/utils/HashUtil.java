package com.tim.nuspacker.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class HashUtil {
    public static byte[] hashSHA2(byte[] data){
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0x20]; 
        }       
       
        return sha256.digest(data);
    }
    
    public static byte[] hashSHA1(byte[] data){
        MessageDigest sha1;
        try {
            sha1 = MessageDigest.getInstance("SHA1");            
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return new byte[0x14]; 
        }       
       
        return sha1.digest(data);
    }
    
    public static byte[] hashSHA1(File file) {
        return hashSHA1(file, 0);
    }
    
    public static byte[] hashSHA1(File file,int aligmnent) {
        byte[] hash = new byte[0x14];
        MessageDigest sha1 = null;
        try {
            InputStream in = new FileInputStream(file);
            sha1 = MessageDigest.getInstance("SHA1");  
            hash = hash(sha1,in,file.length(),0x8000,aligmnent);
        } catch (NoSuchAlgorithmException | FileNotFoundException e) {            
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  
       
        return hash;
    }
    
    public static byte [] hash(MessageDigest digest, InputStream in,long inputSize, int bufferSize,int alignment) throws IOException {
        long target_size = alignment == 0 ? inputSize: Utils.align(inputSize, alignment);
        long cur_position = 0;
        int inBlockBufferRead = 0;
        byte[] blockBuffer = new byte[bufferSize];
        ByteArrayBuffer overflow = new ByteArrayBuffer(bufferSize);
        do{
            if(cur_position + bufferSize > inputSize){
                long expectedSize = (inputSize - cur_position);
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
                
                inBlockBufferRead = Utils.getChunkFromStream(in,blockBuffer,overflow,expectedSize);
                buffer.put(Arrays.copyOfRange(blockBuffer, 0, inBlockBufferRead));
                blockBuffer = buffer.array();
                inBlockBufferRead = bufferSize;
            }else{
                int expectedSize = bufferSize;
                inBlockBufferRead = Utils.getChunkFromStream(in,blockBuffer,overflow,expectedSize);
            }
            digest.update(blockBuffer, 0, inBlockBufferRead);
            cur_position += inBlockBufferRead;
            
        }while(cur_position < target_size && (inBlockBufferRead == bufferSize));
       
        in.close();

        return digest.digest();
    }

    
}
