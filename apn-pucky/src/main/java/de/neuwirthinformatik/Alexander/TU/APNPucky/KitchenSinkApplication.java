/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

//https://github.com/Perfare/AssetStudio
//http://alexsadonis.blogspot.com/
//http://devxdevelopment.com/UnpackerFAQ
//https://7daystodie.com/forums/showthread.php?22675-Unity-Assets-Bundle-Extractor
//http://forum.xentax.com/viewtopic.php?f=10&t=10085
//https://gameart.eu.org/reverse-engineering-decompiling-unity3d-files/

package de.neuwirthinformatik.Alexander.TU.APNPucky;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.core.io.ResourceLoader;
import org.json.JSONObject;
import com.cloudinary.*;
import com.cloudinary.utils.ObjectUtils;

import ai.api.AIConfiguration;
import ai.api.AIDataService;

@SpringBootApplication
public class KitchenSinkApplication {

	public static ResourceLoader resourceLoader;
	public static Render render;
	public static Cloudinary cloudinary = new Cloudinary(
			ObjectUtils.asMap("cloud_name", System.getenv("CLOUDINARY_CLOUD_NAME"), "api_key",
					System.getenv("CLOUDINARY_API_KEY"), "api_secret", System.getenv("CLOUDINARY_API_SECRET")));
	public static AIDataService dataService = new AIDataService(new AIConfiguration(System.getenv("DIALOGFLOW_CLIENT_ACCESS_TOKEN")));
	static Path downloadedContentDir;

	public static void main(String[] args) throws IOException {
		downloadedContentDir = Files.createTempDirectory("line-bot");
		SpringApplication.run(KitchenSinkApplication.class, args);
	}

}
