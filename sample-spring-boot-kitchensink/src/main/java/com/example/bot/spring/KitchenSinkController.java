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

package com.example.bot.spring;

import java.io.IOException;

import com.example.bot.spring.Card.CardType;
import com.example.bot.spring.Card.CardCategory;
import com.example.bot.spring.Card.CardInstance;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.io.ByteStreams;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.DatetimePickerAction;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.action.URIAction;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.FollowEvent;
import com.linecorp.bot.model.event.JoinEvent;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.UnfollowEvent;
import com.linecorp.bot.model.event.message.AudioMessageContent;
import com.linecorp.bot.model.event.message.ImageMessageContent;
import com.linecorp.bot.model.event.message.LocationMessageContent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.event.source.RoomSource;
import com.linecorp.bot.model.event.source.Source;
import com.linecorp.bot.model.message.AudioMessage;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.ImagemapMessage;
import com.linecorp.bot.model.message.LocationMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.imagemap.ImagemapArea;
import com.linecorp.bot.model.message.imagemap.ImagemapBaseSize;
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
import com.linecorp.bot.model.message.imagemap.URIImagemapAction;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.message.template.CarouselColumn;
import com.linecorp.bot.model.message.template.CarouselTemplate;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.message.template.ImageCarouselColumn;
import com.linecorp.bot.model.message.template.ImageCarouselTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import org.json.JSONObject;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
public class KitchenSinkController {
	@Autowired
	private LineMessagingClient lineMessagingClient;

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
		TextMessageContent message = event.getMessage();
		handleTextContent(event.getReplyToken(), event, message);
	}

	@EventMapping
	public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
		handleSticker(event.getReplyToken(), event.getMessage());
	}

	@EventMapping
	public void handleLocationMessageEvent(MessageEvent<LocationMessageContent> event) {
		LocationMessageContent locationMessage = event.getMessage();
		reply(event.getReplyToken(), new LocationMessage(locationMessage.getTitle(), locationMessage.getAddress(),
				locationMessage.getLatitude(), locationMessage.getLongitude()));
	}

	@EventMapping
	public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
		// You need to install ImageMagick
		handleHeavyContent(event.getReplyToken(), event.getMessage().getId(), responseBody -> {
			DownloadedContent jpg = saveContent("jpg", responseBody);
			DownloadedContent previewImg = createTempFile("jpg");
			system("convert", "-resize", "240x", jpg.path.toString(), previewImg.path.toString());
			reply(event.getReplyToken(), new ImageMessage(jpg.getUri(), jpg.getUri()));
		});
	}

	@EventMapping
	public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
		handleHeavyContent(event.getReplyToken(), event.getMessage().getId(), responseBody -> {
			DownloadedContent mp4 = saveContent("mp4", responseBody);
			reply(event.getReplyToken(), new AudioMessage(mp4.getUri(), 100));
		});
	}

	@EventMapping
	public void handleVideoMessageEvent(MessageEvent<VideoMessageContent> event) throws IOException {
		// You need to install ffmpeg and ImageMagick.
		handleHeavyContent(event.getReplyToken(), event.getMessage().getId(), responseBody -> {
			DownloadedContent mp4 = saveContent("mp4", responseBody);
			DownloadedContent previewImg = createTempFile("jpg");
			system("convert", mp4.path + "[0]", previewImg.path.toString());
			reply(event.getReplyToken(), new VideoMessage(mp4.getUri(), previewImg.uri));
		});
	}

	@EventMapping
	public void handleUnfollowEvent(UnfollowEvent event) {
		log.info("unfollowed this bot: {}", event);
	}

	@EventMapping
	public void handleFollowEvent(FollowEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Got followed event");
	}

	@EventMapping
	public void handleJoinEvent(JoinEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Joined " + event.getSource());
	}

	@EventMapping
	public void handlePostbackEvent(PostbackEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Got postback data " + event.getPostbackContent().getData() + ", param "
				+ event.getPostbackContent().getParams().toString());
	}

	@EventMapping
	public void handleBeaconEvent(BeaconEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Got beacon message " + event.getBeacon().getHwid());
	}

	@EventMapping
	public void handleOtherEvent(Event event) {
		log.info("Received message(Ignored): {}", event);
	}

	private void reply(@NonNull String replyToken, @NonNull Message message) {
		reply(replyToken, Collections.singletonList(message));
	}

	private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			BotApiResponse apiResponse = lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
			log.info("Sent messages: {}", apiResponse);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void replyText(@NonNull String replyToken, @NonNull String message) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		if (message.length() > 1000) {
			message = message.substring(0, 1000 - 2) + "……";
		}
		this.reply(replyToken, new TextMessage(message));
	}

	private void handleHeavyContent(String replyToken, String messageId,
			Consumer<MessageContentResponse> messageConsumer) {
		final MessageContentResponse response;
		try {
			response = lineMessagingClient.getMessageContent(messageId).get();
		} catch (InterruptedException | ExecutionException e) {
			reply(replyToken, new TextMessage("Cannot get image: " + e.getMessage()));
			throw new RuntimeException(e);
		}
		messageConsumer.accept(response);
	}

	private void handleSticker(String replyToken, StickerMessageContent content) {
		reply(replyToken, new StickerMessage(content.getPackageId(), content.getStickerId()));
	}

	private static String[][] alias = new String[][]{
		{"materials","mats","build","-m","-b"},
	};
	private static String[][] help = new String[][]{
		{"help","apn bot help"},
		{"tuo","tuo version"},
		{"alias","enlist alias"},
		{"materials","displays materials for card"},
		{"new","displays latest quads"},
		{"xml","show the date of xmls"},
		{"update","reload xmls"},
		{"roadmap","tu roadmap + link"},
	};
	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws Exception {
		String ptext = content.getText();//.toLowerCase();
		if (!ptext.startsWith("apn ")) {
			return;
		}
		log.info("Got text message from {}: {}", replyToken, ptext);
		//alias
		for(String[] sa :  alias)
		{
			for(String s : sa)
			{
				ptext.replaceAll("apn " + s, "apn " + sa[0]);
				log.info("Current ptext", replyToken, ptext);
			}
		}

		String[] arr = ptext.split("apn ");
		if (arr.length < 2) {
			return;
		}
		String[] args = arr[1].split(" ");
		String text = args[0];
		switch (text) {
		case "help": {
			String rep = "TU line chat bot apn:\n" + "Usage: apn {option}\n" 	+"\nOptions:\n" ;
			for(String[] sa : help) rep += "\t - "+sa[0]+": \n\t\t\t\t\t\t "+sa[1]+"\n";
		this.replyText(replyToken, rep);
			break;
		}
		case "alias": {
			String msg = "Alias list:\n\n";
			for(String[] sa : alias)
			{
				msg += "\t - "+sa[0]+": \n\t\t\t\t\t\t ";
				for(int i = 1; i < sa.length;i++) msg += "'" + sa[i]+"'', ";
				msg += "\n";
			}
			this.replyText(replyToken, msg);
			break;
		}
		case "xml": {
			this.replyText(replyToken, "load new dev-xml @" + Data.xml_time);
			break;
		}
		case "update": {
			Data.init();
			this.replyText(replyToken, "load new dev-xml @" + Data.xml_time);
			break;
		}
		case "new": {
			int number = 5;
			if(!(args.length < 2))
			{
				number = Integer.parseInt(args[1]);
			}
			if(number > 20)number = 20;
			String msg = "";
			for(int i = 1; i < Data.distinct_cards.length && number > 0; i++)
			{
				Card c = Data.distinct_cards[Data.distinct_cards.length-i];
				if(c.fusion_level == 2)
				{
					msg += c.toString() + "\n";
					number--;
				}
			}
			this.replyText(replyToken,msg);
			break;
		}
		case "materials": {
			if(args.length < 2)
			{
				this.replyText(replyToken, "Please pass a card with: apn materials {card}");
				break;
			}
			String card_name = ptext.split("apn materials ")[1];
			CardInstance ci = Data.getCardInstanceByNameAndLevel(card_name);
			if(ci == null || ci == CardInstance.NULL)
			{
				this.replyText(replyToken, "Unknown card: '" + card_name + "'");
				break;
			}
			this.replyText(replyToken, "Card: " + ci + "\n" +
			"Fused by: \n[" + Data.getInvString(Data.getIDsFromCardInstances(ci.getMaterials())).replaceAll("\n",", ") + "]\n\n" +
			"Required Materials (" + ci.getCostFromLowestMaterials() + " SP): \n[" + Data.getInvString(Data.getIDsFromCardInstances(ci.getLowestMaterials().toArray(new CardInstance[] {}))).replaceAll("\n",", ") + "]\n");
			break;
		}
		case "card": {
			if(args.length < 2)
			{
				this.replyText(replyToken, "Please pass a card with: apn card {card}");
				break;
			}
			CardInstance ci = Data.getCardInstanceByNameAndLevel(ptext.split("apn card ")[1]);
			this.replyText(replyToken, "Card: " + ci.toString() + " [" +ci.getID() + "]");
			break;
		}
		case "roadmap": {
			this.replyText(replyToken, "Roadmap feature, soon tm");
			break;
		}
		case "tuo": {
			String json = Wget.wGet(
					"https://api.github.com/repos/APN-Pucky/tyrant_optimize/releases/latest");
			String tuojson = json.replaceAll("\n", "");
			JSONObject tuo = new JSONObject(tuojson);
			String tag_name = (tuo).getString("tag_name");
			String commit = tuo.getString("name");
			this.replyText(replyToken, "TUO VERSION= " + tag_name + " - " + commit);
			break;
		}
		case "profile": {
			String userId = event.getSource().getUserId();
			if (userId != null) {
				lineMessagingClient.getProfile(userId).whenComplete((profile, throwable) -> {
					if (throwable != null) {
						this.replyText(replyToken, throwable.getMessage());
						return;
					}

					this.reply(replyToken, Arrays.asList(new TextMessage("Display name: " + profile.getDisplayName()),
							new TextMessage("Status message: " + profile.getStatusMessage())));

				});
			} else {
				this.replyText(replyToken, "Bot can't use profile API without user ID");
			}
			break;
		}
		case "bye": {
			Source source = event.getSource();
			if (source instanceof GroupSource) {
				this.replyText(replyToken, "Leaving group");
				lineMessagingClient.leaveGroup(((GroupSource) source).getGroupId()).get();
			} else if (source instanceof RoomSource) {
				this.replyText(replyToken, "Leaving room");
				lineMessagingClient.leaveRoom(((RoomSource) source).getRoomId()).get();
			} else {
				this.replyText(replyToken, "Bot can't leave from 1:1 chat");
			}
			break;
		}
		case "confirm": {
			ConfirmTemplate confirmTemplate = new ConfirmTemplate("Do it?", new MessageAction("Yes", "Yes!"),
					new MessageAction("No", "No!"));
			TemplateMessage templateMessage = new TemplateMessage("Confirm alt text", confirmTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "buttons": {
			String imageUrl = createUri("/static/buttons/1040.jpg");
			ButtonsTemplate buttonsTemplate = new ButtonsTemplate(imageUrl, "My button sample", "Hello, my button",
					Arrays.asList(new URIAction("Go to line.me", "https://line.me"),
							new PostbackAction("Say hello1", "hello こんにちは"),
							new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
							new MessageAction("Say message", "Rice=米")));
			TemplateMessage templateMessage = new TemplateMessage("Button alt text", buttonsTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "carousel": {
			String imageUrl = createUri("/static/buttons/1040.jpg");
			CarouselTemplate carouselTemplate = new CarouselTemplate(Arrays.asList(
					new CarouselColumn(imageUrl, "hoge", "fuga",
							Arrays.asList(new URIAction("Go to line.me", "https://line.me"),
									new URIAction("Go to line.me", "https://line.me"),
									new PostbackAction("Say hello1", "hello こんにちは"))),
					new CarouselColumn(imageUrl, "hoge", "fuga",
							Arrays.asList(new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
									new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"),
									new MessageAction("Say message", "Rice=米"))),
					new CarouselColumn(imageUrl, "Datetime Picker", "Please select a date, time or datetime",
							Arrays.asList(
									new DatetimePickerAction("Datetime", "action=sel", "datetime", "2017-06-18T06:15",
											"2100-12-31T23:59", "1900-01-01T00:00"),
									new DatetimePickerAction("Date", "action=sel&only=date", "date", "2017-06-18",
											"2100-12-31", "1900-01-01"),
									new DatetimePickerAction("Time", "action=sel&only=time", "time", "06:15", "23:59",
											"00:00")))));
			TemplateMessage templateMessage = new TemplateMessage("Carousel alt text", carouselTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "image_carousel": {
			String imageUrl = createUri("/static/buttons/1040.jpg");
			ImageCarouselTemplate imageCarouselTemplate = new ImageCarouselTemplate(Arrays.asList(
					new ImageCarouselColumn(imageUrl, new URIAction("Goto line.me", "https://line.me")),
					new ImageCarouselColumn(imageUrl, new MessageAction("Say message", "Rice=米")),
					new ImageCarouselColumn(imageUrl, new PostbackAction("言 hello2", "hello こんにちは", "hello こんにちは"))));
			TemplateMessage templateMessage = new TemplateMessage("ImageCarousel alt text", imageCarouselTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "imagemap":
			this.reply(replyToken,
					new ImagemapMessage(createUri("/static/rich"), "This is alt text", new ImagemapBaseSize(1040, 1040),
							Arrays.asList(
									new URIImagemapAction("https://store.line.me/family/manga/en",
											new ImagemapArea(0, 0, 520, 520)),
									new URIImagemapAction("https://store.line.me/family/music/en",
											new ImagemapArea(520, 0, 520, 520)),
									new URIImagemapAction("https://store.line.me/family/play/en",
											new ImagemapArea(0, 520, 520, 520)),
									new MessageImagemapAction("URANAI!", new ImagemapArea(520, 520, 520, 520)))));
			break;
		case "flex":
			this.reply(replyToken, new ExampleFlexMessageSupplier().get());
			break;
		case "quickreply":
			this.reply(replyToken, new MessageWithQuickReplySupplier().get());
			break;
		default:
			//log.info("Returns echo message {}: {}", replyToken, text);
			this.replyText(replyToken, "Unknown command.");
			break;
		}
	}

	private static String createUri(String path) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().path(path).build().toUriString();
	}

	private void system(String... args) {
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		try {
			Process start = processBuilder.start();
			int i = start.waitFor();
			log.info("result: {} =>  {}", Arrays.toString(args), i);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (InterruptedException e) {
			log.info("Interrupted", e);
			Thread.currentThread().interrupt();
		}
	}

	private static DownloadedContent saveContent(String ext, MessageContentResponse responseBody) {
		log.info("Got content-type: {}", responseBody);

		DownloadedContent tempFile = createTempFile(ext);
		try (OutputStream outputStream = Files.newOutputStream(tempFile.path)) {
			ByteStreams.copy(responseBody.getStream(), outputStream);
			log.info("Saved {}: {}", ext, tempFile);
			return tempFile;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static DownloadedContent createTempFile(String ext) {
		String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
		Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
		tempFile.toFile().deleteOnExit();
		return new DownloadedContent(tempFile, createUri("/downloaded/" + tempFile.getFileName()));
	}

	@Value
	public static class DownloadedContent {
		Path path;
		String uri;
	}
}
