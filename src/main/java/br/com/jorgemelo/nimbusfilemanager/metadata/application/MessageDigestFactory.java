package br.com.jorgemelo.nimbusfilemanager.metadata.application;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@FunctionalInterface
interface MessageDigestFactory {

	MessageDigest getInstance(String algorithm) throws NoSuchAlgorithmException;
}