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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cloudinary.utils.ObjectUtils;
import com.example.bot.spring.Card.CardInstance;
import com.google.common.io.ByteStreams;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
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
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
@SuppressWarnings({ "rawtypes", "unchecked" })
public class KitchenSinkController {
	@Autowired
	private LineMessagingClient lineMessagingClient;

	private Random r = new Random();
	@Autowired
	public ResourceLoader rl;

	@PreDestroy
	public void finalEnd() {
		pushText("Uab4d6ff3d59aee3ce4869e894ca4e337", "Stop " + System.getenv("HEROKU_RELEASE_VERSION"));
	}

	@PostConstruct
	public void init() {
		KitchenSinkApplication.resourceLoader = rl;
		Data.init();
		KitchenSinkApplication.render = new Render();
		System.out.println("APN " + System.getenv("HEROKU_RELEASE_VERSION"));
		pushText("Uab4d6ff3d59aee3ce4869e894ca4e337", "Start " + System.getenv("HEROKU_RELEASE_VERSION"));
	}

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
		/*
		 * reply(event.getReplyToken(), new LocationMessage(locationMessage.getTitle(),
		 * locationMessage.getAddress(), locationMessage.getLatitude(),
		 * locationMessage.getLongitude()));
		 */
	}

	@EventMapping
	public void handleImageMessageEvent(MessageEvent<ImageMessageContent> event) throws IOException {
		// You need to install ImageMagick
		/*
		 * handleHeavyContent(event.getReplyToken(), event.getMessage().getId(),
		 * responseBody -> { DownloadedContent jpg = saveContent("jpg", responseBody);
		 * DownloadedContent previewImg = createTempFile("jpg"); system("convert",
		 * "-resize", "240x", jpg.path.toString(), previewImg.path.toString());
		 * reply(event.getReplyToken(), new ImageMessage(jpg.getUri(), jpg.getUri()));
		 * });
		 */
	}

	@EventMapping
	public void handleAudioMessageEvent(MessageEvent<AudioMessageContent> event) throws IOException {
		/*
		 * handleHeavyContent(event.getReplyToken(), event.getMessage().getId(),
		 * responseBody -> { DownloadedContent mp4 = saveContent("mp4", responseBody);
		 * reply(event.getReplyToken(), new AudioMessage(mp4.getUri(), 100)); });
		 */
	}

	@EventMapping
	public void handleVideoMessageEvent(MessageEvent<VideoMessageContent> event) throws IOException {
		// You need to install ffmpeg and ImageMagick.
		/*
		 * handleHeavyContent(event.getReplyToken(), event.getMessage().getId(),
		 * responseBody -> { DownloadedContent mp4 = saveContent("mp4", responseBody);
		 * DownloadedContent previewImg = createTempFile("jpg"); system("convert",
		 * mp4.path + "[0]", previewImg.path.toString()); reply(event.getReplyToken(),
		 * new VideoMessage(mp4.getUri(), previewImg.uri)); });
		 */
	}

	@EventMapping
	public void handleUnfollowEvent(UnfollowEvent event) {
		log.info("unfollowed this bot: {}", event);
	}

	@EventMapping
	public void handleFollowEvent(FollowEvent event) {
		String replyToken = event.getReplyToken();
		log.info("followed this bot: {}", event);
		// this.replyText(replyToken, "Got followed event");
	}

	@EventMapping
	public void handleJoinEvent(JoinEvent event) {
		String replyToken = event.getReplyToken();
		this.replyText(replyToken, "Hi, Usage: 'apn help'");
	}

	@EventMapping
	public void handlePostbackEvent(PostbackEvent event) {
		String replyToken = event.getReplyToken();
		// this.replyText(replyToken, "Got postback data " +
		// event.getPostbackContent().getData() + ", param "
		// + event.getPostbackContent().getParams().toString());
	}

	@EventMapping
	public void handleBeaconEvent(BeaconEvent event) {
		String replyToken = event.getReplyToken();
		// this.replyText(replyToken, "Got beacon message " +
		// event.getBeacon().getHwid());
	}

	@EventMapping
	public void handleOtherEvent(Event event) {
		log.info("Received message(Ignored): {}", event);
	}

	private void push(@NonNull String id, @NonNull Message message) {
		push(id, Collections.singletonList(message));
	}

	private void push(@NonNull String id, @NonNull List<Message> messages) {
		try {
			BotApiResponse apiResponse = lineMessagingClient.pushMessage(new PushMessage(id, messages)).get();
			log.info("Sent push messages: {}", apiResponse);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void pushLongText(@NonNull String id, @NonNull String message) {

	}

	private void pushText(@NonNull String id, @NonNull String message) {
		log.info("Pushing to '" + id + "'");
		if (id.isEmpty()) {
			throw new IllegalArgumentException("id must not be empty");
		}
		/*
		 * if (message.length() > 1000) { message = message.substring(0, 1000 - 2) +
		 * "……"; }
		 */
		this.push(id, new TextMessage(message.trim()));
	}

	private void reply(@NonNull String replyToken, @NonNull Message message) {
		reply(replyToken, Collections.singletonList(message));
	}

	private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
		try {
			BotApiResponse apiResponse = lineMessagingClient.replyMessage(new ReplyMessage(replyToken, messages)).get();
			log.info("Sent reply messages: {}", apiResponse);
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	private void replyText(@NonNull String replyToken, @NonNull String message) {
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		/*
		 * if (message.length() > 1000) { message = message.substring(0, 1000 - 2) +
		 * "……"; }
		 */
		this.reply(replyToken, new TextMessage(message.trim()));
	}

	private void handleHeavyContent(String replyToken, String messageId,
			Consumer<MessageContentResponse> messageConsumer) {
		/*
		 * final MessageContentResponse response; try { response =
		 * lineMessagingClient.getMessageContent(messageId).get(); } catch
		 * (InterruptedException | ExecutionException e) { reply(replyToken, new
		 * TextMessage("Cannot get image: " + e.getMessage())); throw new
		 * RuntimeException(e); } messageConsumer.accept(response);
		 */
	}

	private static int[][] stickerids = new int[][] { { 1, 103 }, { 1, 102 }, { 1, 101 }, { 1, 100 }, { 1, 109 },
			{ 1, 405 }, { 1, 406 }, { 1, 402 }, { 1, 116 }, { 1, 404 }, { 1, 411 }, { 1, 420 }, { 2, 47 }, { 2, 39 },
			{ 2, 161 }, { 2, 165 }, { 2, 30 }, { 2, 28 }, { 2, 34 }, { 2, 526 }, { 2, 502 }, { 2, 520 }, { 2, 521 },
			{ 2, 512 }, { 2, 178 }, { 2, 179 }, { 3, 225 }, { 3, 226 }, { 3, 223 }, { 3, 224 }, { 3, 227 }, { 3, 220 },
			{ 3, 221 }, { 3, 222 }, { 3, 253 }, { 4, 287 }, { 4, 285 }, { 4, 283 }, { 4, 279 }, { 4, 281 }, { 4, 280 },
			{ 4, 288 }, { 4, 300 }, { 4, 291 }, { 4, 298 }, { 4, 608 }, { 4, 282 } };

	private void handleSticker(String replyToken, StickerMessageContent content) {
		int pi = r.nextInt(stickerids.length);
		if (Math.random() > 0.9)
			reply(replyToken, new StickerMessage("" + stickerids[pi][0], "" + stickerids[pi][1]));
	}

	private static String[][] alias = new String[][] { { "materials", "mats", "build", "-m", "-b" },
			{ "today", "current" }, { "change", "release" }, { "update", "-u" }, { "list", "search" },
			{ "card", "-c", "show", "display" }, { "battlegroundeffect", "bge" }, { "random", "fun", "lol", "lul" },
			{ "joke", "geek" }, { "nude", "nudes" },{"dad","daddy","dev", "share","forward","bug"}, { "version", "-v" }, { "help", "\\?", "-h" },
			{ "options", "-o", "opts" }, };
	private static String[][] help = new String[][] { { "card", "display a card" }, { "icard", "display a card with image" },
			{ "materials", "displays materials for card" }, { "new", "displays latest quads" }, {"bge", "display a bge"}, {"skill", "display a skill"},
			{ "roadmap", "tu roadmap + link" }, { "current", "current tu event" }, { "next", "next tu event" },
			{ "release", "next tu release" }, { "tuo", "tuo version" }, { "options", "apn bot options" }, };
	private static String[][] large_help = new String[][] { { "xml", "show the date of xmls" },
			{ "update", "reload xmls" }, { "alias", "enlist alias" }, { "random", "random" }, { "joke", "geeky joke" },
			{ "fail", "fail gif" }, { "art", "art image" }, { "pic", "some image" }, { "xkcd", "xkcd image" },
			{ "meme", "meme image" }, { "version", "version of this bot" }, };

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws Exception {
		final String ftext = content.getText().toLowerCase();
		if (!ftext.startsWith("apn ")) {
			return;
		}
		String userId = event.getSource().getUserId();
		if (userId != null)
			lineMessagingClient.getProfile(userId).whenComplete((profile, throwable) -> {
				if (throwable != null) {
					log.info("Got text error:{}", throwable.getMessage());
					return;
				}
				log.info("Got text message from {}: {}", profile.getDisplayName(), ftext);
			});
		else
			log.info("Got text message from {}: {}", "Unknown", ftext);
		String ptext = ftext;
		// alias
		for (String[] sa : alias) {
			for (String s : sa) {
				ptext = ptext.replaceAll("apn " + s, "apn " + sa[0]);
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
			String rep = "TU line chat bot apn:\n" + "Usage: 'apn {option}'\n" + "\nOptions:\n";
			for (String[] sa : help)
				rep += "\t - " + sa[0] + ": \n\t\t\t\t\t\t " + sa[1] + "\n";
			this.replyText(replyToken, rep);
			break;
		}
		case "options": {
			String rep = "TU line chat bot apn:\n" + "Usage: 'apn {option}'\n" + "\nOptions:\n";
			for (String[] sa : help)
				rep += "\t - " + sa[0] + ": \n\t\t\t\t\t\t " + sa[1] + "\n";
			for (String[] sa : large_help)
				rep += "\t - " + sa[0] + ": \n\t\t\t\t\t\t " + sa[1] + "\n";
			this.replyText(replyToken, rep);
			break;
		}
		case "version": {
			String msg = "APN " + System.getenv("HEROKU_RELEASE_VERSION");
			this.replyText(replyToken, msg);
			break;
		}
		case "alias": {
			String msg = "Alias list:\n\n";
			for (String[] sa : alias) {
				msg += "\t - " + sa[0] + ": \n\t\t\t\t\t\t ";
				for (int i = 1; i < sa.length; i++)
					msg += "'" + sa[i] + "', ";
				msg += "\n";
			}
			this.replyText(replyToken, msg);
			break;
		}
		case "xml": {
			this.replyText(replyToken, "load dev-xml @" + Data.xml_time);
			break;
		}
		case "update": {
			Data.init();
			this.replyText(replyToken, "load new dev-xml @" + Data.xml_time);
			break;
		}
		case "new": {
			int skip = 0;
			int number = 5;
			int offset = Data.all_cards.length;
			if (!(args.length < 2)) {
				if (!args[1].matches("\\d+")) {
					switch (args[1]) {
					case "help": {
						this.replyText(replyToken,
								"Usage: 'apn new {dom(inion)/struct(ure)/commander/cmd/assault} {number} {skip}'");
						return;
					}
					case "dom":
					case "dominion": {
						offset = 55001;
						break;
					}
					case "struct":
					case "structure": {
						offset = 25000;
						break;
					}
					case "cmd":
					case "commander": {
						offset = 30000;
						break;
					}
					default: {
						offset = Data.all_cards.length;
						break;
					}
					}
					if (!(args.length < 3) && args[2].matches("\\d+")) {
						number = Integer.parseInt(args[2]);
						if (!(args.length < 4) && args[3].matches("\\d+")) {
							skip = Integer.parseInt(args[3]);
						}
					}
				} else {
					number = Integer.parseInt(args[1]);
					if (!(args.length < 3) && args[2].matches("\\d+")) {
						skip = Integer.parseInt(args[2]);
					}
				}

			}
			if (number > 10)
				number = 10;
			String msg = "";
			ArrayList<Card> printed = new ArrayList<Card>();
			for (int i = 1; i < Data.all_cards.length && number > 0; i++) {
				Card c = Data.all_cards[offset - i];
				if (c != null && c.fusion_level == 2 && !printed.contains(c)
						&& !c.getName().toLowerCase().startsWith("test")
						&& !c.getName().toLowerCase().startsWith("revolt ranger")
						&& !c.getName().toLowerCase().startsWith("cephalodjinn")) {
					printed.add(c);
					if (skip > 0) {
						skip--;
					} else {

						this.pushText(event.getSource().getSenderId(), c.description());
						// msg += + "\n---------------------------------------"+"\n";
						number--;
					}
				}
			}
			// msg = StringUtil.removeLastCharacter(msg,42);
			// this.replyText(replyToken,msg);
			break;
		}
		case "skill": {
			if (args.length < 2) {
				this.replyText(replyToken, "Please pass a name with: 'apn skill {name}'");
				break;
			}
			String req = ptext.split("apn skill ")[1].toLowerCase();
			if (Data.skill_desc.containsKey(req)) {
				this.replyText(replyToken, "'" + req + "': " + Data.skill_desc.get(req));
			} else {
				this.replyText(replyToken, "Unknown skill: '" + req + "'");
			}
			break;
		}
		case "battlegroundeffect": {
			if (args.length < 2) {
				this.replyText(replyToken, "Please pass a card with: 'apn bge {bge}'");
				break;
			}
			String req = ptext.split("apn battlegroundeffect ")[1];
			// TODO CHECK github bges.txt => Number yes no
			String url = getBGEUrl(req);
			if (url == null) {
				String lbge = Wget
						.wGet("https://raw.githubusercontent.com/APN-Pucky/tyrant_optimize/merged/data/bges.txt");
				String[] dbges = lbge.split("\n");
				for (int i = 0; i < dbges.length; i++) {
					String b = dbges[i];
					String[] inf = b.split(":");
					if (inf.length > 1 && StringUtil.containsIgnoreSpecial(inf[0], req)) {
						this.replyText(replyToken, b);
						break;
					}
				}
				this.replyText(replyToken, "Unknown bge: '" + req + "'");
				break;
			}
			String map = getFirstKongPost(url);
			map = map.substring(StringUtil.indexOfIgnoreCard(map, req));
			String ret = "";
			String[] lines = map.split("\n");
			lines[0] = lines[0].replaceAll("\\*+", "");
			for (String l : lines) {
				if (l.contains("will start") || l.contains("will affect all modes"))
					break;
				ret += l + "\n\n";
			}
			this.replyText(replyToken, ret);
			break;
		}
		case "list": {
			if (args.length < 2) {
				this.replyText(replyToken, "Please pass a name with: 'apn list {name}'");
				break;
			}
			String req = ptext.split("apn list ")[1];
			String rep = "card search: '" + req + "'\n\n";
			for (Card c : Data.distinct_cards) {
				if (StringUtil.containsIgnoreSpecial(c.getName(), req)) {
					rep += c.getName() + "\n";
					if (rep.length() > 1000) {
						rep += "..........EOM..........";
						break;
					}
				}
			}
			this.replyText(replyToken, rep);
			break;
		}
		case "materials": {
			if (args.length < 2) {
				this.replyText(replyToken, "Please pass a card with: 'apn materials {card}'");
				break;
			}
			String card_name = ptext.split("apn materials ")[1];
			CardInstance ci = getCardInstance(card_name);
			if (ci == null || ci == CardInstance.NULL) {
				this.replyText(replyToken, "Unknown card: '" + card_name + "'");
				break;
			}
			this.replyText(replyToken, "Card: " + ci + "\n" + "Fused by: \n["
					+ StringUtil.removeLastCharacter(Data.getInvString(Data.getIDsFromCardInstances(ci.getMaterials()))
							.replaceAll("\n", ", "), 2)
					+ "]\n\n" + "Required Materials (" + ci.getCostFromLowestMaterials() + " SP): \n["
					+ StringUtil.removeLastCharacter(Data
							.getInvString(Data
									.getIDsFromCardInstances(ci.getLowestMaterials().toArray(new CardInstance[] {})))
							.replaceAll("\n", ", "), 2)
					+ "]\n");
			break;
		}
		case "icard": {
			if (args.length < 2) {
				this.replyText(replyToken, "Please pass a card with: 'apn icard {card}'");
				break;
			}
			String req = ptext.split("apn icard ")[1];
			CardInstance ci = getCardInstance(req);

			if (ci == null || ci == CardInstance.NULL) {
				this.replyText(replyToken, "Unknown card: '" + req + "'");
				break;
			}
			BufferedImage bi = KitchenSinkApplication.render.render(ci);
			DownloadedContent d = createTempFile("png");
			ImageIO.write(bi, "png", d.path.toFile());
			try {
				Map uploadResult = KitchenSinkApplication.cloudinary.uploader().upload(d.uri, ObjectUtils.emptyMap());
				Files.deleteIfExists(d.path);
				String perm_uri = (String) uploadResult.get("secure_url");
				this.reply(replyToken, new ImageMessage(perm_uri, perm_uri));
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
			this.reply(replyToken, new ImageMessage(d.uri, d.uri));
			break;
		}
		case "card": {
			if (args.length < 2) {
				this.replyText(replyToken, "Please pass a card with: 'apn card {card}'");
				break;
			}
			String req = ptext.split("apn card ")[1];
			CardInstance ci = getCardInstance(req);

			if (ci == null || ci == CardInstance.NULL) {
				this.replyText(replyToken, "Unknown card: '" + req + "'");
				break;
			}
			this.replyText(replyToken, ci.description());
			break;
		}
		case "change": {
			String map = getRoadMap();
			String rep = "";
			String[] sections = map.split("\\*\\*");
			Date min = null;
			for (int i = 3; i < sections.length; i += 2) {
				String title = sections[i];
				String msg = sections[i + 1];
				String[] split = msg.split("\\*");
				String date = split[1];
				String conten = split[2];
				for (int j = 3; j < split.length; j++)
					conten += "*" + split[j];
				conten = conten.trim();
				// rep += title + "\n";
				// rep += date + "\n\n";

				SimpleDateFormat parser = new SimpleDateFormat("yyyyMMMMMMMMM d");
				String[] dates = date.split("-");
				String d1 = dates[0];
				d1 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d1.trim(), "(\\d)(st|nd|rd|th)",
						"$1");

				Date dd1 = parser.parse(d1);
				if ((min == null || dd1.before(min)) && dd1.after(Calendar.getInstance().getTime())) {
					min = dd1;
					rep = title + "\n" + date + "\n\n" + conten;
				}
				// System.out.println(dd1);
				if (dates.length > 1) {
					String d2 = dates[1];
					d2 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d2.trim(),
							"(\\d)(st|nd|rd|th)", "$1");
					Date dd2 = parser.parse(d2);
					Calendar cal = Calendar.getInstance();
					cal.setTime(dd2);
					cal.add(Calendar.DATE, 1);
					dd2 = cal.getTime();
					// System.out.println(dd2);
					// if(Calendar.getInstance().getTime().before(dd2) &&
					// Calendar.getInstance().getTime().after(dd1)) System.out.println(title + "\n"
					// + date + "\n\n" + content);
				}
			}
			this.replyText(replyToken, rep);
			break;
		}

		case "next": {
			String map = getRoadMap();
			String rep = "";
			String[] sections = map.split("\\*\\*");
			Date min = null;
			for (int i = 3; i < sections.length; i += 2) {
				String title = sections[i];
				String msg = sections[i + 1];
				String[] split = msg.split("\\*");
				String date = split[1];
				String conten = split[2];
				for (int j = 3; j < split.length; j++)
					conten += "*" + split[j];
				conten = conten.trim();
				// rep += title + "\n";
				// rep += date + "\n\n";

				SimpleDateFormat parser = new SimpleDateFormat("yyyyMMMMMMMMM d");
				String[] dates = date.split("-");
				String d1 = dates[0];
				d1 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d1.trim(), "(\\d)(st|nd|rd|th)",
						"$1");

				Date dd1 = parser.parse(d1);
				// System.out.println(dd1);
				if (dates.length > 1) {
					String d2 = dates[1];
					d2 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d2.trim(),
							"(\\d)(st|nd|rd|th)", "$1");
					Date dd2 = parser.parse(d2);
					Calendar cal = Calendar.getInstance();
					cal.setTime(dd2);
					cal.add(Calendar.DATE, 1);
					dd2 = cal.getTime();
					if ((min == null || dd1.before(min)) && dd1.after(Calendar.getInstance().getTime())) {
						min = dd1;
						rep = title + "\n" + date + "\n\n" + conten;
					}
					// System.out.println(dd2);
					// if(Calendar.getInstance().getTime().before(dd2) &&
					// Calendar.getInstance().getTime().after(dd1)) System.out.println(title + "\n"
					// + date + "\n\n" + content);
				}
			}
			this.replyText(replyToken, rep);
			break;
		}
		case "today": {
			String map = getRoadMap();
			String rep = "";
			String[] sections = map.split("\\*\\*");
			for (int i = 3; i < sections.length; i += 2) {
				String title = sections[i];
				String msg = sections[i + 1];
				String[] split = msg.split("\\*");
				String date = split[1];
				String conten = split[2];
				for (int j = 3; j < split.length; j++)
					conten += "*" + split[j];
				conten = conten.trim();
				// rep += title + "\n";
				// rep += date + "\n\n";

				SimpleDateFormat parser = new SimpleDateFormat("yyyyMMMMMMMMM d");
				String[] dates = date.split("-");
				String d1 = dates[0];
				d1 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d1.trim(), "(\\d)(st|nd|rd|th)",
						"$1");

				Date dd1 = parser.parse(d1);
				if (dates.length > 1) {
					String d2 = dates[1];
					d2 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d2.trim(),
							"(\\d)(st|nd|rd|th)", "$1");
					Date dd2 = parser.parse(d2);
					Calendar cal = Calendar.getInstance();// jump one day for borders
					cal.setTime(dd2);
					cal.add(Calendar.DATE, 1);
					dd2 = cal.getTime();
					if (Calendar.getInstance().getTime().before(dd2) && Calendar.getInstance().getTime().after(dd1))
						rep += title + "\n" + date + "\n\n" + conten + "\n\n";
				}

			}
			if (rep.equals(""))
				rep = "No event today";
			this.replyText(replyToken, rep);
			break;
		}
		case "roadmap": {
			String url = getRoadMapUrl();
			String map = getRoadMap(url);

			if (!(args.length < 2) && args[1].equals("full")) {
				this.replyText(replyToken, map + "\n\n" + url);
				break;
			}

			String rep = "";
			String[] sections = map.split("\\*\\*");
			for (int i = 1; i < sections.length; i += 2) {
				String title = sections[i];
				String msg = sections[i + 1];
				String date = msg.split("\\*")[1];
				rep += title + "\n";
				rep += date + "\n\n";
			}
			rep += url;
			this.replyText(replyToken, rep);

			// this.pushText(event.getSource().getSenderId(), map);
			break;
		}
		case "tuo": {
			String json = Wget.wGet("https://api.github.com/repos/APN-Pucky/tyrant_optimize/releases/latest");
			String tuojson = json.replaceAll("\n", "");
			JSONObject tuo = new JSONObject(tuojson);
			String tag_name = (tuo).getString("tag_name");
			String commit = tuo.getString("name");
			this.replyText(replyToken, "TUO " + tag_name + " - " + commit);
			break;
		}
		case "dark": {
			String url = getRedditTagUrl("OffensiveMemes");
			this.reply(replyToken, new ImageMessage(url, url));
			break;
		}
		case "nude": {
			String url = getRedditTagUrl("hardwareporn");
			this.reply(replyToken, new ImageMessage(url, url));
			break;
		}
		case "rip": {
			String url = getRedditTagUrl("techsupportgore");
			this.reply(replyToken, new ImageMessage(url, url));
			break;
		}
		case "random": {
			Random r = new Random();
			String[] opt = new String[] { "art", "pic", "joke", "gif", "fail", "meme", "xkcd", "insult", "mama" };
			switch (opt[r.nextInt(opt.length)]) {
			case "art":
				art(replyToken);
				break;
			case "pic":
				pic(replyToken);
				break;
			case "insult":
				insult(replyToken);
				break;
			case "joke":
				joke(replyToken);
				break;
			case "mama":
				mama(replyToken);
				break;
			case "gif":
				gif(replyToken, new String[] { args[0] });
				break;
			case "fail":
				fail(replyToken);
				break;
			case "meme":
				meme(replyToken);
				break;
			case "xkcd":
				xkcd(replyToken);
				break;
			}
			break;
		}
		case "art": {
			art(replyToken);
			break;
		}
		case "pic": {
			pic(replyToken);
			break;
		}
		case "reddit": {
			reddit(replyToken, args);
			break;
		}
		case "gif": {
			gif(replyToken, args);
			break;
		}
		case "fail": {
			fail(replyToken);
			break;
		}
		case "insult": {
			insult(replyToken);
			break;
		}
		case "mama": {
			mama(replyToken);
			break;
		}
		case "joke": {
			joke(replyToken);
			break;
		}
		case "xkcd": {
			xkcd(replyToken);
			break;
		}
		case "meme": {
			meme(replyToken);
			break;
		}
		case "dad": {
			String imageUrl = createUri("/static/buttons/hannibal.jpg");
			ButtonsTemplate buttonsTemplate = new ButtonsTemplate(imageUrl, "DR_F3LL", "TU LINE chat bot",
					Arrays.asList(new URIAction("Visit APN-Pucky", "line://ti/p/%40xdc0493y"),
							new URIAction("Visit DR_F3LL", "line://ti/p/%40archi_85"),
							new MessageAction("Random", "apn random"),
							new MessageAction("Help", "apn help"),
							new URIAction("Share", "line://nv/recommendOA/%40xdc0493y")

					));
			TemplateMessage templateMessage = new TemplateMessage("Button alt text", buttonsTemplate);
			this.reply(replyToken, templateMessage);
			break;
		}
		case "flex":
			this.reply(replyToken, new ExampleFlexMessageSupplier().get());
			break;
		case "quickreply":
			this.reply(replyToken, new MessageWithQuickReplySupplier().get());
			break;
		case "say": {
			this.replyText(replyToken, content.getText().substring(7));
			break;
		}
		case "echo": {
			this.replyText(replyToken, content.getText().substring(8));
			break;
		}
		default:
			log.info("Unknown command {}: {}", replyToken, text);
			this.replyText(replyToken, "Unknown command '" + text + "'.\nUse apn help for a list of options.");
			break;
		}
		System.gc();
	}

	private void reddit(String replyToken, String tag) {
		String url = getRedditTagUrl(tag);
		this.reply(replyToken, new ImageMessage(url, url));
	}

	private void reddit(String replyToken, String[] args) {
		String url = null;
		if (!(args.length < 2)) {
			url = getRedditTagUrl(args[1]);
		} else {
			url = getRedditTagUrl("pics");
		}
		this.reply(replyToken, new ImageMessage(url, url));
	}

	private void art(String replyToken) {
		reddit(replyToken, "Art");
	}

	private void pic(String replyToken) {
		reddit(replyToken, "pic");
	}

	private void gif(String replyToken, String tag) {
		Pair<String, String> url = getGIFTagUrl(tag);
		this.reply(replyToken, new VideoMessage(url.t, url.u));
	}

	private void gif(String replyToken, String[] args) {
		Pair<String, String> url = null;
		if (!(args.length < 2)) {
			url = getGIFTagUrl(args[1]);
			if (url.u == url.t && url.u == null) {
				this.replyText(replyToken, "No gif for '" + args[1] + "'");
				return;
			}
		} else {
			url = getGIFUrl();
		}
		this.reply(replyToken, new VideoMessage(url.t, url.u));
	}

	private void fail(String replyToken) {
		gif(replyToken, "fail");
	}

	private void insult(String replyToken) {
		String msg = Wget.sendGet("https://insult.mattbas.org/api/insult");
		// System.out.println(msg);
		msg = msg.replaceAll("\"", "").replaceAll("&quot;", "\"");
		// System.out.println(msg);
		this.replyText(replyToken, msg);
	}

	private void mama(String replyToken) {
		String msg = Wget.sendGet("http://api.yomomma.info/");
		JSONObject j = new JSONObject(msg);
		msg = j.getString("joke");
		this.replyText(replyToken, msg);
	}

	private void joke(String replyToken) {
		String msg = Wget.sendGet("https://geek-jokes.sameerkumar.website/api");
		// System.out.println(msg);
		msg = msg.replaceAll("\"", "").replaceAll("&quot;", "\"");
		// System.out.println(msg);
		this.replyText(replyToken, msg);
	}

	private void meme(String replyToken) {
		String url = getMEMEUrl();
		this.reply(replyToken, new ImageMessage(url, url));
	}

	private void xkcd(String replyToken) {
		String url = getXKCDUrl();
		this.reply(replyToken, new ImageMessage(url, url));
	}

	private static String getRedditTagUrl(String tag) {
		String json = Wget.sendGet("https://www.reddit.com/r/" + tag + "/random.json");
		String url = new JSONArray(json).getJSONObject(0).getJSONObject("data").getJSONArray("children")
				.getJSONObject(0).getJSONObject("data").getString("url");
		if (!url.matches(".*\\.(jpg|png).*")) {
			if (url.matches(".*imgur.*") && !url.matches(".*gallery.*"))
				url += ".jpg";
			else
				url = getRedditTagUrl(tag); // only png+fig
		}
		if (!url.matches(".*https://.*")) {
			if (url.matches(".*imgur.*"))
				url = url.replace("http://", "https://");
			else
				url = getRedditTagUrl(tag); // only https
		}
		log.info("Reddit image url: " + url);

		return url;
	}

	private static Pair<String, String> getGIFTagUrl(String tag) {
		String xkcd = Wget
				.wGet("https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&tag=" + tag + "&rating=pg-13");
		JSONObject json = new JSONObject(xkcd);
		JSONObject data = json.optJSONObject("data");
		if (data == null)
			return new Pair<String, String>(null, null);
		String url = data.getString("image_mp4_url");
		String url2 = data.getString("image_url");
		return new Pair<String, String>(url, url2);
	}

	private static Pair<String, String> getGIFUrl() {
		String xkcd = Wget.wGet("https://api.giphy.com/v1/gifs/random?api_key=dc6zaTOxFJmzC&rating=pg-13");
		JSONObject json = new JSONObject(xkcd);
		JSONObject data = json.optJSONObject("data");
		String url = data.getString("image_mp4_url");
		String url2 = data.getString("image_url");
		return new Pair<String, String>(url, url2);
	}

	private static String getMEMEUrl() {
		String xkcd = Wget.wGet("http://www.quickmeme.com/random");
		String[] lines = xkcd.split("\n");
		String fin = "";
		ArrayList<String> urls = new ArrayList<String>();
		for (String l : lines) {
			if (l.contains("class=\"post-image\" src=\"")) {
				fin = l;
				String url = fin.substring(fin.indexOf("src=\"") + 5).split("\"")[0].trim();
				urls.add(url);
			}
		}
		Random r = new Random();
		String url = urls.get(r.nextInt(urls.size()));
		DownloadedContent img = createTempFile("jpg");
		Wget.wGet(img.path.toString(), url);
		try {
			Map uploadResult = KitchenSinkApplication.cloudinary.uploader().upload(img.uri, ObjectUtils.emptyMap());
			Files.deleteIfExists(img.path);
			return (String) uploadResult.get("secure_url");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return "null";
		return img.uri;
	}

	private static String getXKCDUrl() {
		String xkcd = Wget.wGet("https://c.xkcd.com/random/comic/");
		String[] lines = xkcd.split("\n");
		String fin = "";
		for (String l : lines) {
			if (l.contains("Image URL (for hotlinking/embedding): ")) {
				fin = l;
				break;
			}
		}
		String url = fin.substring(fin.indexOf(": ") + 1).trim();
		return url;
	}

	private static String getBGEUrl(String bge) {
		String general = Wget.wGet(
				"https://www.kongregate.com/forums/2468-general/topics/387545-q-a-account-sharing-etiquette-faq-support-player-made-guides");
		general = general.substring(general.indexOf("Global Battleground Effects"),
				general.indexOf("Restore Information"));
		String[] lines = general.split("\n");
		String fin = "";
		for (String l : lines) {
			if (StringUtil.containsIgnoreSpecial(l, bge)) {
				fin = l;
				break;
			}
		}
		String url = fin.replaceFirst(".*(https?://www\\.kongregate\\.com/forums/2468-general/topics/\\d+).*", "$1");
		// String url =
		// fin.substring(fin.indexOf("href=\"http://www.kongregte.com/forums/2468-general")
		// + 6, fin.indexOf("\">"));
		if (url.matches("https?://www\\.kongregate\\.com/forums/2468-general/topics/\\d+"))
			return url;
		return null;
	}

	private static String getRoadMapUrl() {
		String general = Wget.wGet("https://www.kongregate.com/forums/2468-general");
		String[] lines = general.split("\n");
		String fin = "";
		for (String l : lines) {
			if (l.contains("Roadmap")) {
				fin = l;
				break;
			}
		}
		String url = "https://www.kongregate.com"
				+ fin.substring(fin.indexOf("href=\"/forums/2468-general") + 6, fin.indexOf("\">[Dev]"));
		return url;
	}

	private static String getFirstKongPost(String url) {
		String road = Wget.wGet(url);

		String map = road.substring(road.indexOf("<div class=\"raw_post\""));
		map = map.substring(map.indexOf(">") + 1);
		map = map.substring(0, map.indexOf("</div>"));
		return map;
	}

	private static String getRoadMap(String url) {
		return getFirstKongPost(url);
	}

	private static String getRoadMap() {
		return getRoadMap(getRoadMapUrl());
	}

	private static CardInstance getCardInstance(String idorname) {
		if (idorname.matches("\\d+")) {
			return new CardInstance(Integer.parseInt(idorname));
		} else {
			return Data.getCardInstanceByNameAndLevel(StringUtil.capitalizeOnlyFirstLetters(idorname));
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
