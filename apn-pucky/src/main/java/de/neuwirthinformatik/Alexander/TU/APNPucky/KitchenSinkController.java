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

package de.neuwirthinformatik.Alexander.TU.APNPucky;

import java.awt.FontFormatException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.cloudinary.Api;
import com.cloudinary.utils.ObjectUtils;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.MessageContentResponse;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.MessageAction;
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
import com.linecorp.bot.model.event.message.TextMessageContent.Mention;
import com.linecorp.bot.model.event.message.TextMessageContent.Mention.Mentionee;
import com.linecorp.bot.model.event.message.VideoMessageContent;
import com.linecorp.bot.model.event.source.GroupSource;
import com.linecorp.bot.model.message.ImageMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.VideoMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import de.neuwirthinformatik.Alexander.TU.Basic.Card;
import de.neuwirthinformatik.Alexander.TU.Basic.Card.CardInstance;
import de.neuwirthinformatik.Alexander.TU.Basic.Card.CardType;
import de.neuwirthinformatik.Alexander.TU.Basic.Deck;
import de.neuwirthinformatik.Alexander.TU.Basic.Gen;
import de.neuwirthinformatik.Alexander.TU.Basic.GlobalData;
import de.neuwirthinformatik.Alexander.TU.Basic.SkillSpec;
import de.neuwirthinformatik.Alexander.TU.Basic.XMLParser;
import de.neuwirthinformatik.Alexander.TU.Render.DownloadedContent;
import de.neuwirthinformatik.Alexander.TU.Render.Render;
import de.neuwirthinformatik.Alexander.TU.TUO.TUO;
import de.neuwirthinformatik.Alexander.TU.TUO.TUO.Param.Dom;
import de.neuwirthinformatik.Alexander.TU.TUO.TUO.Param.Mode;
import de.neuwirthinformatik.Alexander.TU.TUO.TUO.Param.OP;
import de.neuwirthinformatik.Alexander.TU.TUO.TUO.Param.Order;
import de.neuwirthinformatik.Alexander.TU.TUO.TUO.Result;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LineMessageHandler
@SuppressWarnings({ "rawtypes", "unchecked" })
public class KitchenSinkController {
	@Autowired
	private LineMessagingClient lineMessagingClient;

	private Random r = new Random();
	public String tuo_prefix = "";

	@Autowired
	public ResourceLoader rl;

	@PreDestroy
	public void finalEnd() {
		pushText("Uab4d6ff3d59aee3ce4869e894ca4e337", "Stop " + System.getenv("HEROKU_RELEASE_VERSION"));
	}

	private void cloudinaryCleanup() {
		try {
			Api api = KitchenSinkApplication.cloudinary.api();
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
			Map map = null;
			while (map == null || map.containsKey("next_cursor"))
				map = api.resources(map == null ? ObjectUtils.asMap("max_results", 500)
						: ObjectUtils.asMap("max_results", 500, "next_cursor", map.get("next_cursor")));
			ArrayList al = (ArrayList) map.get("resources");
			for (Object o : al) {
				Map m = (Map) o;
				String date = (String) m.get("created_at");
				Date create = parser.parse(date);
				cal.setTime(create);
				cal.add(Calendar.DATE, 5);
				Date create7 = cal.getTime();
				// System.out.println(create);
				if (create7.before(Calendar.getInstance().getTime())) {
					ArrayList a = new ArrayList(1);
					a.add(m.get("public_id"));
					// System.out.println("Deleting: " + a.get(0));
					Map ma = api.deleteResources(a, ObjectUtils.emptyMap());
					// System.out.println(ma);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@PostConstruct
	public void init() {
		KitchenSinkApplication.resourceLoader = rl;
		r.setSeed(System.currentTimeMillis());
		GlobalData.init(false);

		cloudinaryCleanup();
		try {
			KitchenSinkApplication.render = new LineRender();
		} catch (FontFormatException | IOException e1) {
			// TODO Auto-generated catch block
			System.out.println("No Render");
			e1.printStackTrace();
		}
		System.out.println("APN " + System.getenv("HEROKU_RELEASE_VERSION"));
		try {
			pushText("Uab4d6ff3d59aee3ce4869e894ca4e337", "Start " + System.getenv("HEROKU_RELEASE_VERSION"));
			if (System.getenv("HEROKU_RELEASE_VERSION") == null) {// local tests
				pushText("Uab4d6ff3d59aee3ce4869e894ca4e337", Gen.gen());
			}
		} catch (Exception e) {
			System.out.println("Too many pushes?");
			e.printStackTrace();
		}
	}

	// @EventListener(ApplicationReadyEvent.class)
	public void downloadXML() throws IOException {
		DownloadedContent dc = createTempFolder("data");
		tuo_prefix = dc.getPath().getParent().toAbsolutePath().toString() + "/";
		System.out.println("TUO_DIR " + tuo_prefix + " with ");
		GlobalData.xml.downloadXML(false, dc.getPath().toAbsolutePath().toString());
		Files.walk(dc.getPath()).filter(Files::isRegularFile).forEach(System.out::println);
	}

	@EventMapping
	public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws Exception {
		TextMessageContent message = event.getMessage();
		if (tuo_prefix.equals(""))
			downloadXML();
		handleTextContent(event.getReplyToken(), event, message);
	}

	@EventMapping
	public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
		new APNMessageHandler(lineMessagingClient, event, "");
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
		new APNMessageHandler(lineMessagingClient, event, "");
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
			try {
				BotApiResponse apiResponse = lineMessagingClient.pushMessage(new PushMessage(id, messages)).get();
				log.info("Sent push messages: {}", apiResponse);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				// throw new RuntimeException(e);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void pushLongText(@NonNull String id, @NonNull String message) {
		String[] lines = message.split("\n");
		String cur = "";
		for (String l : lines) {
			if (cur.length() + l.length() < 1500) {
				cur += l + "\n";
			} else {
				pushText(id, cur); // TODO check for order issues
				cur = l + "\n"; // one long line here, but broken, maybe TODO split new line @ spaces
			}
		}
		pushText(id, cur);
	}

	private void pushText(@NonNull String id, @NonNull String message) {
		if (message.equals(""))
			return;
		log.info("Pushing to '" + id + "'");
		if (id.isEmpty()) {
			throw new IllegalArgumentException("id must not be empty");
		}

		if (message.length() > 2000) {
			message = message.substring(0, 2000 - 2) + "……";
		}
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
		if (message.equals(""))
			return;
		if (replyToken.isEmpty()) {
			throw new IllegalArgumentException("replyToken must not be empty");
		}
		if (message.length() > 2000) {
			message = message.substring(0, 2000 - 2) + "……";
		}

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

	private static final int[][] stickerids = new int[][] { { 1, 103 }, { 1, 102 }, { 1, 101 }, { 1, 100 }, { 1, 109 },
			{ 1, 405 }, { 1, 406 }, { 1, 402 }, { 1, 116 }, { 1, 404 }, { 1, 411 }, { 1, 420 }, { 2, 47 }, { 2, 39 },
			{ 2, 161 }, { 2, 165 }, { 2, 30 }, { 2, 28 }, { 2, 34 }, { 2, 526 }, { 2, 502 }, { 2, 520 }, { 2, 521 },
			{ 2, 512 }, { 2, 178 }, { 2, 179 }, { 3, 225 }, { 3, 226 }, { 3, 223 }, { 3, 224 }, { 3, 227 }, { 3, 220 },
			{ 3, 221 }, { 3, 222 }, { 3, 253 }, { 4, 287 }, { 4, 285 }, { 4, 283 }, { 4, 279 }, { 4, 281 }, { 4, 280 },
			{ 4, 288 }, { 4, 300 }, { 4, 291 }, { 4, 298 }, { 4, 608 }, { 4, 282 } };

	private void handleSticker(String replyToken, StickerMessageContent content) {
		if (Math.random() > 0.99)
			sticker(replyToken);
	}

	private static final String[][] help = new String[][] { { "card", "display a card" },
			{ "icard", "display a card with image" }, { "materials", "displays materials for card" },
			{ "new", "displays latest quads" }, { "bge", "display a bge" }, { "skill", "display a skill" },
			{ "roadmap", "tu roadmap + link" }, { "current", "current tu event" }, { "next", "next tu event" },
			{ "release", "next tu release" }, { "generate", "generates a new TU card" }, { "tuo", "tuo version" },
			{ "options", "apn bot options" }, };
	private static final String[][] large_help = new String[][] { { "xml", "show the date of xmls" },
			{ "update", "reload xmls" }, { "alias", "enlist alias" }, { "random", "random" }, { "joke", "geeky joke" },
			{ "fail", "fail gif" }, { "art", "art image" }, { "pic", "some image" }, { "xkcd", "xkcd image" },
			{ "meme", "meme image" }, { "version", "version of this bot" }, };

	private static final String[][] amazon_coin_urls = new String[][] {
			{ "USA", "https://www.amazon.com/Amazon-50-000-Coins/dp/B018HB6E80" },
			{ "DE", "https://www.amazon.de/dp/B018GWRCV8" }, { "UK", "https://www.amazon.co.uk/dp/B018GRDG5O" },
			{ "FR", "https://www.amazon.fr/dp/B018EZT2YM" } };

	private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws Exception {
		final APNMessageHandler apn = new APNMessageHandler(lineMessagingClient, replyToken, event, content);
		// final String ftext = content.getText().toLowerCase();
		if (!apn.getArg(0).equals("apn")) {
			if (apn.getMessage().contains("*")) {
				String[] ss = apn.getMessage().split("\\*");
				for (int i = 1; i < ss.length; i += 2) {
					if (ss[i].contains(",")) {
						Deck ci = GlobalData.constructDeck(ss[i]);
						if (i == 1)
							this.reply(apn.getReplyToken(), genDeckMessage(true, ci));
						else
							this.push(apn.getSenderId(), genDeckMessage(true, ci));
					} else {
						CardInstance ci = GlobalData.getCardInstance(ss[i]);
						if (i == 1)
							this.reply(apn.getReplyToken(), genCardInstanceMessage(true, ci));
						else
							this.push(apn.getSenderId(), genCardInstanceMessage(true, ci));
					}
				}
			}

			return;
		}

		// String userId = event.getSource().getUserId();
		if (apn.getUserId() != null)
			lineMessagingClient.getProfile(apn.getUserId()).whenComplete((profile, throwable) -> {
				if (throwable != null) {
					log.info("Got text error:{}", throwable.getMessage());
					return;
				}
				log.info("Got text message from {}: {}", profile.getDisplayName(), apn.getMessage());
			});
		else
			log.info("Got text message from {}: {}", "Unknown", apn.getMessage());

		switch (apn.getArg(1)) {
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
			for (String[] sa : APNMessageHandler.alias) {
				msg += "\t - " + sa[0] + ": \n\t\t\t\t\t\t ";
				for (int i = 1; i < sa.length; i++)
					msg += "'" + sa[i] + "', ";
				msg += "\n";
			}
			this.replyText(replyToken, msg);
			break;
		}
		case "xml": {
			this.replyText(replyToken, "load dev-xml @" + GlobalData.xml_time);
			break;
		}
		case "update": {
			GlobalData.init(false);
			this.replyText(replyToken, "load new dev-xml @" + GlobalData.xml_time);
			break;
		}
		case "inew": {
			case_new(apn, true);
			break;
		}
		case "new": {
			case_new(apn, false);
			break;
		}
		case "generate": {
			case_generate(apn);
			break;
		}
		case "who": {
			case_who(apn);
			break;
		}
		case "fight": {
			case_fight(apn);
			break;
		}
		case "skill": {
			if (apn.getArgs().length < 3) {
				this.replyText(replyToken, "Please pass a name with: 'apn skill {name}'");
				break;
			}
			if (apn.equals(2, "full")) {
				// TODO
			}
			String req = apn.getFrom(2);// ptext.split("apn skill ")[1].toLowerCase().trim();
			if (GlobalData.skill_desc.containsKey(req)) {
				this.replyText(replyToken, "'" + req + "': " + GlobalData.skill_desc.get(req));
			} else {
				this.replyText(replyToken, "Unknown skill: '" + req + "'");
			}
			break;
		}
		case "battlegroundeffect": {
			case_bge(apn);
			break;
		}
		case "list": {
			case_list(apn, false);
			break;
		}
		case "imaterials": {
			case_materials(apn, true);
			break;
		}
		case "materials": {
			case_materials(apn, false);
			break;
		}
		case "icard": {
			case_card(apn, true);
			break;
		}
		case "card": {
			case_card(apn, false);
			break;
		}
		case "ideck": {
			case_deck(apn, true);
			break;
		}
		case "deck": {
			case_deck(apn, false);
			break;
		}
		case "change": {
			case_roadmaps(apn);
			break;
		}
		case "next": {
			case_roadmaps(apn);
			break;
		}
		case "today": {
			case_roadmaps(apn);
			break;
		}
		case "roadmap": {
			String url = Kong.getRoadMapUrl();
			String map = Kong.getRoadMap(url);

			if (apn.getArgs().length >= 3 && apn.equals(2, "full")) {
				this.replyText(replyToken, map + "\n\n" + url);
				break;
			}

			String rep = "";
			String[] sections = map.split("\\*\\*");
			for (int i = 3; i < sections.length; i += 2) {
				String title = sections[i];
				String msg = sections[i + 1];
				String date = msg.split("\\n")[2];
				rep += title + "\n";
				rep += date + "\n\n";
			}
			rep += url;
			this.replyText(replyToken, rep);

			// this.pushText(event.getSource().getSenderId(), map);
			break;
		}
		case "coins": {
			String msg = "";
			for (String[] a : amazon_coin_urls) {
				int price = getAmazonCoinPrice(a[1]);
				msg += a[0] + ":\t " + price + "(" + (100 - ((double) price) / 5) + "%)\n";
			}
			this.replyText(replyToken, msg);
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
		/*
		 * case "dark": { String url = getRedditTagUrl("OffensiveMemes");
		 * this.reply(replyToken, new ImageMessage(url, url)); break; }
		 */
		case "nude": {
			String url = getRedditTagUrl("hardwareporn");
			this.reply(apn.getReplyToken(), new ImageMessage(URI.create(url), URI.create(url)));
			break;
		}
		case "rip": {
			String url = getRedditTagUrl("techsupportgore");
			this.reply(apn.getReplyToken(), new ImageMessage(URI.create(url), URI.create(url)));
			break;
		}
		case "info": {
			this.replyText(apn.getReplyToken(), "Wanna buy drugs?");
			this.pushText(apn.getUserId(), "Group id: " + apn.getSenderId());
			this.pushText(apn.getUserId(), "User id: " + apn.getUserId());
			break;
		}
		case "sender": {
			this.replyText(apn.getReplyToken(), apn.getSenderId());
			break;
		}
		case "user": {
			this.replyText(apn.getReplyToken(), apn.getUserId());
			break;
		}
		case "push": {
			if (apn.getArgs().length < 3) {
				this.replyText(apn.getReplyToken(), "Usage: apn push id msg");
				break;
			}
			this.pushText(apn.getArg(2, true), apn.getFrom(3, true));
			break;
		}
		case "roulette": {
			String msg = "";
			if (apn.getEvent().getSource() instanceof GroupSource) {
				String[] names = apn.getRecentNames();
				int shot = r.nextInt(names.length);
				for (int i = 0; i < names.length; i++) {
					if (i == shot) {
						String t = new String[] { "died", "was not so lucky", "has been shot", "shot himself",
								"commited suicide", "is dead" }[r.nextInt(6)];
						msg += names[i] + " " + t + "\n";
					} else {
						String t = new String[] { "lives to tell the tale", "survived", "was lucky today",
								"is alive" }[r.nextInt(4)];
						msg += names[i] + " " + t + "\n";
					}
				}
			} else {
				msg = "Only available in Groups";
			}
			replyText(apn.getReplyToken(), msg);
			break;
		}
		case "random": {
			Random r = new Random();
			String[] opt = new String[] { "art", "pic", "joke", "gif", "fail", "meme", "xkcd", "insult", "mama",
					"chicken", "donkey", "sticker" };
			switch (opt[r.nextInt(opt.length)]) {
			case "art":
				art(apn);
				break;
			case "pic":
				pic(apn);
				break;
			case "sticker":
				sticker(apn.getReplyToken());
				break;
			case "insult":
				insult(apn);
				break;
			case "joke":
				joke(apn);
				break;
			case "mama":
				mama(apn);
				break;
			case "gif":
				gif(apn, "random");
				break;
			case "fail":
				fail(apn);
				break;
			case "meme":
				meme(apn);
				break;
			case "xkcd":
				xkcd(apn);
				break;
			/*
			 * case "poop": poop(apn); break;
			 */
			case "chicken":
				chicken(apn);
				break;
			case "donkey":
				donkey(apn);
				break;
			}
			break;
		}
		case "poop": {
			poop(apn);
			break;
		}
		case "chicken": {
			chicken(apn);
			break;
		}
		case "donkey":
			donkey(apn);
			break;
		case "art": {
			art(apn);
			break;
		}
		case "pic": {
			pic(apn);
			break;
		}
		case "reddit": {
			reddit(apn);
			break;
		}
		case "gif": {
			gif(apn);
			break;
		}
		case "fail": {
			fail(apn);
			break;
		}
		case "insult": {

			insult(apn);
			break;
		}
		case "mama": {
			mama(apn);
			break;
		}
		case "joke": {
			joke(apn);
			break;
		}
		case "xkcd": {
			xkcd(apn);
			break;
		}
		case "meme": {
			meme(apn);
			break;
		}
		case "dad": {
			String imageUrl = createUri("/static/buttons/hannibal.jpg");
			ButtonsTemplate buttonsTemplate = new ButtonsTemplate(URI.create(imageUrl), "DR_F3LL", "TU LINE chat bot",
					Arrays.asList(URIA.create("Visit APN-Pucky", "line://ti/p/%40xdc0493y"),
							URIA.create("Visit DR_F3LL", "line://ti/p/cGOI7BBPeE"), // TODO replace with dr_F3ll whe
																					// works
							new MessageAction("Random", "apn random"),
							// new MessageAction("Help", "apn help"),
							URIA.create("Share", "line://nv/recommendOA/@xdc0493y")

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
			log.info("Unknown command {}: {}", replyToken, apn.getArg(1));
			case_default(apn);
			// this.replyText(replyToken, "Unknown command '" + apn.getArg(1) + "'.\nUse apn
			// help for a list of options.");
			break;
		}
		// apn = null;
		System.gc();
	}

	private void case_default(APNMessageHandler apn) {
		try {
			AIRequest request = new AIRequest(apn.getFrom(1));

			AIResponse response = KitchenSinkApplication.dataService.request(request);

			if (response.getStatus().getCode() == 200) {
				this.replyText(apn.getReplyToken(), response.getResult().getFulfillment().getSpeech());
			} else {
				System.err.println(response.getStatus().getErrorDetails());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void case_fight(APNMessageHandler apn) {
		UserProfileResponse y_upr = null;
		try {
			y_upr = apn.getLmc().getProfile(apn.getUserId()).join();
		} catch (Exception e) {
			e.printStackTrace();
			this.replyText(apn.getReplyToken(), "Error, are you my friend?");
			return;
		}
		String y_pic_url = y_upr.getPictureUrl().toString();
		String y_name = y_upr.getDisplayName();
		int y_seed = (y_name + y_upr.getStatusMessage()).hashCode();
		DownloadedContent y_pic = createTempFile("jpg");
		Wget.wGet(y_pic.getPath().toString(), y_pic_url);

		UserProfileResponse e_upr = null;
		if (!apn.getFrom(2).equals("self")) {
			Mention me = apn.getContent().getMention();
			if (me == null) {
				this.replyText(apn.getReplyToken(), "You must @mention your enemy.");
				return;
			}
			List<Mentionee> ms = me.getMentionees();
			if (ms.size() != 1) {
				this.replyText(apn.getReplyToken(), "You can only fight against a single person.");
				return;
			}
			Mentionee m = ms.get(0);
			try {
				e_upr = apn.getLmc().getProfile(m.getUserId()).join();
			} catch (Exception e) {
				e.printStackTrace();
				this.replyText(apn.getReplyToken(), "Error, " + apn.getFrom(2) + " is not my friend.");
				return;
			}
		} else {
			e_upr = y_upr;
		}
		String e_pic_url = e_upr.getPictureUrl().toString();
		String e_name = e_upr.getDisplayName();
		int e_seed = (e_name + e_upr.getStatusMessage()).hashCode();
		DownloadedContent e_pic = createTempFile("jpg");
		Wget.wGet(e_pic.getPath().toString(), e_pic_url);

		CardInstance y_com = Gen.genCardInstance(y_name + "_COM", y_seed, true);
		CardInstance y_dom = Gen.genCardInstance(y_name + "_DOM", y_seed, CardType.DOMINION);
		CardInstance y_ass = Gen.genCardInstance(y_name, y_seed, CardType.ASSAULT);
		CardInstance e_com = Gen.genCardInstance(e_name + "_COM", e_seed, true);
		CardInstance e_dom = Gen.genCardInstance(e_name + "_DOM", e_seed, CardType.DOMINION);
		CardInstance e_ass = Gen.genCardInstance(e_name, e_seed, CardType.ASSAULT);

		synchronized (tuo_prefix) {
			TUO.tuo = "tuo";
			try {
				IOUtils.write("<?xml version='1.0' encoding='UTF-8'?>\r\n" + "<root></root>",
						new FileOutputStream(new File(tuo_prefix + "data/cards_section_"
								+ (XMLParser.CARD_SECTIONS_COUNT_DOWNLOAD + 1) + ".xml")),
						"UTF-8");
				GlobalData.xml.appendToCardSection(tuo_prefix + "", XMLParser.CARD_SECTIONS_COUNT_DOWNLOAD + 1,
						y_com.getCard());
				GlobalData.xml.appendToCardSection(tuo_prefix + "", XMLParser.CARD_SECTIONS_COUNT_DOWNLOAD + 1,
						y_dom.getCard());
				GlobalData.xml.appendToCardSection(tuo_prefix + "", XMLParser.CARD_SECTIONS_COUNT_DOWNLOAD + 1,
						y_ass.getCard());
				GlobalData.xml.appendToCardSection(tuo_prefix + "", XMLParser.CARD_SECTIONS_COUNT_DOWNLOAD + 1,
						e_com.getCard());
				GlobalData.xml.appendToCardSection(tuo_prefix + "", XMLParser.CARD_SECTIONS_COUNT_DOWNLOAD + 1,
						e_dom.getCard());
				GlobalData.xml.appendToCardSection(tuo_prefix + "", XMLParser.CARD_SECTIONS_COUNT_DOWNLOAD + 1,
						e_ass.getCard());
			} catch (Exception e) {
				e.printStackTrace();
				this.replyText(apn.getReplyToken(), "XML generation failed :(");
				return;
			}
			Result res = sim(y_name + "_COM," + y_name + "_DOM," + y_name + "#10",
					e_name + "_COM," + e_name + "_DOM," + e_name + "#10");
			BufferedImage img = null;
			try {
				img = new LineRender().renderScore(res, y_com, y_dom, y_ass, e_com, e_dom, e_ass,
						y_pic.getPath().toFile(), e_pic.getPath().toFile());
			} catch (FontFormatException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (img != null) {
				this.reply(apn.getReplyToken(), cacheImageMessage(img));
			} else {
				this.replyText(apn.getReplyToken(), "Result: " + res.WINS + "/" + res.STALLS + "/" + res.LOSSES);
			}
		}

	}

	public Result sim(String deck1, String deck2) {
		TUO.Param p = new TUO.Param(deck1, deck2, OP.sim, Order.random, Mode.pvp_defense, Dom.dom_maxed, "", 100, 0);
		System.out.println("TUO prefix" + tuo_prefix);
		p.flags = "prefix " + tuo_prefix;
		Result r = TUO.sim(p);
		System.out.println(r.WINS + " " + r.LOSSES);
		return r;
	}

	private void case_who(APNMessageHandler apn) {
		UserProfileResponse upr = null;
		try {
			upr = apn.getLmc().getProfile(apn.getUserId()).join();
		} catch (Exception e) {
			e.printStackTrace();
			this.replyText(apn.getReplyToken(), "Error, are you my friend?");
			return;
		}
		String pic_url = upr.getPictureUrl().toString();
		String name = upr.getDisplayName();
		int seed = (name + upr.getStatusMessage()).hashCode();
		DownloadedContent pic = createTempFile("jpg");
		Wget.wGet(pic.getPath().toString(), pic_url);

		try {
			CardInstance ci = null;
			BufferedImage img = null;
			if (apn.getMessage().contains("dom")) {
				ci = Gen.genCardInstance(name, seed, (c) -> c.getCardType() == CardType.DOMINION);
				img = new LineRender().render(ci, new String[] { "", "", "" }, pic.getPath().toFile(),
						ci.getCardType());
			} else if (apn.getMessage().contains("com")) {
				ci = Gen.genCardInstance(name, seed, (c) -> c.getCardType() == CardType.COMMANDER);
				img = new LineRender().render(ci, new String[] { "", "", "" }, pic.getPath().toFile(),
						ci.getCardType());
			} else if (apn.getMessage().contains("ass")) {
				ci = Gen.genCardInstance(name, seed, (c) -> c.getCardType() == CardType.ASSAULT);
				img = new LineRender().render(ci, new String[] { "", "", "" }, pic.getPath().toFile(),
						ci.getCardType());
			} else if (apn.getMessage().contains("struct")) {
				ci = Gen.genCardInstance(name, seed, (c) -> c.getCardType() == CardType.STRUCTURE);
				img = new LineRender().render(ci, new String[] { "", "", "" }, pic.getPath().toFile(),
						ci.getCardType());
			} else if (apn.getMessage().contains("deck")) {
				CardInstance com = Gen.genCardInstance(name + "_COM", seed, true);
				CardInstance dom = Gen.genCardInstance(name + "_DOM", seed, CardType.DOMINION);
				CardInstance ass = Gen.genCardInstance(name, seed, CardType.ASSAULT);

				img = new LineRender().renderDeck(com, dom, ass, pic.getPath().toFile());
			} else {
				ci = Gen.genCardInstance(name, seed);
				img = new LineRender().render(ci, new String[] { "", "", "" }, pic.getPath().toFile(),
						ci.getCardType());
			}
			if (img != null) {
				this.reply(apn.getReplyToken(), cacheImageMessage(img));
			} else {
				this.replyText(apn.getReplyToken(), "Rendererror...");
			}
		} catch (FontFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void case_materials(APNMessageHandler apn, boolean image) {
		if (apn.getArgs().length < 3) {
			this.replyText(apn.getReplyToken(), "Please pass a card with: 'apn materials {card}'");
			return;
		}
		String card_name = apn.getFrom(2);// ptext.split("apn materials ")[1];
		CardInstance ci = GlobalData.getCardInstance(card_name);
		if (ci == null || ci == CardInstance.NULL) {
			case_list(apn, image);
			// this.replyText(apn.getReplyToken(), "Unknown card: '" + card_name + "'");
		} else {
			this.reply(apn.getReplyToken(), genCardInstanceTreeMessage(image, ci));
		}
	}

	private void case_card(APNMessageHandler apn, boolean image) {
		if (apn.getArgs().length < 3) {
			this.replyText(apn.getReplyToken(), "Please pass a card with: 'apn card {card}'");
			return;
		}
		String req = apn.getFrom(2);// ptext.split("apn card ")[1].trim();
		CardInstance ci = GlobalData.getCardInstance(req);

		if (ci == null || ci == CardInstance.NULL) {
			case_list(apn, image);
			// this.replyText(apn.getReplyToken(), "Unknown card: '" + req + "'");
		} else {
			this.reply(apn.getReplyToken(), genCardInstanceMessage(image, ci));
			if (image)
				recursivePushCISummon(apn, image, ci);
		}
	}

	private void case_deck(APNMessageHandler apn, boolean image) {
		if (apn.getArgs().length < 3) {
			this.replyText(apn.getReplyToken(), "Please pass a deck with: 'apn deck {cards}'");
			return;
		}
		String req = apn.getFrom(2);// ptext.split("apn card ")[1].trim();
		Deck ci = GlobalData.constructDeck(req);

		if (ci == null) {
			case_list(apn, image);
			// this.replyText(apn.getReplyToken(), "Unknown card: '" + req + "'");
		} else {
			this.reply(apn.getReplyToken(), genDeckMessage(image, ci));
		}
	}

	private void case_bge(APNMessageHandler apn) {
		if (apn.getArgs().length < 3) {
			this.replyText(apn.getReplyToken(), "Please pass a card with: 'apn bge {bge}'");
			return;
		}
		String req = apn.getFrom(2);// ptext.split("apn battlegroundeffect ")[1].toLowerCase().trim();
		String url = getBGEUrl(req);
		if (url == null) {
			String lbge = Wget.wGet("https://raw.githubusercontent.com/APN-Pucky/tyrant_optimize/merged/data/bges.txt");
			String[] dbges = lbge.split("\n");
			for (int i = 0; i < dbges.length; i++) {
				String b = dbges[i];
				String[] inf = b.split(":");
				if (inf.length > 1 && StringUtil.containsIgnoreSpecial(inf[0], req)) {
					this.replyText(apn.getReplyToken(), b);
					return;
				}
			}
			this.replyText(apn.getReplyToken(), "Unknown bge: '" + req + "'");
		} else {
			String map = Kong.getFirstKongPost(url);
			map = map.substring(StringUtil.indexOfIgnoreCard(map, req));
			String ret = "";
			String[] lines = map.split("\n");
			lines[0] = lines[0].replaceAll("\\*+", "");
			for (String l : lines) {
				if (l.contains("will start") || l.contains("will affect all modes"))
					break;
				ret += l + "\n\n";
			}
			this.replyText(apn.getReplyToken(), ret);
		}
	}

	private void case_list(APNMessageHandler apn, boolean image) {
		image = false; // HARD DISSABLE image search
		boolean skip = false;
		if (apn.getArgs().length < 3) {
			this.replyText(apn.getReplyToken(), "Please pass a name with: 'apn list {name}'");
			return;
		}
		String req = apn.getFrom(2);// ptext.split("apn list ")[1].trim();
		String rep = "card search: '" + req + "'\n\n";
		boolean changed = false;
		boolean single_ch = true;
		Card cur = null;
		for (Card c : GlobalData.distinct_cards) {
			if (StringUtil.containsIgnoreSpecial(c.getName(), req)) {
				rep += c.getName() + "\n";
				if (!changed)
					changed = true;
				else if (single_ch)
					single_ch = false;
				if (rep.length() > 1000) {
					rep += "..........EOM..........";
					break;
				}
				cur = c;
			}
		}
		if (single_ch && changed && cur != null) {
			this.replyText(apn.getReplyToken(), rep);
			skip = true;
			recursivePushCI(apn, image, GlobalData.getCardInstanceById(cur.getHighestID()));
			return;
		}
		if (!changed) {
			Card close = null;
			int min = -1;
			for (Card c : GlobalData.distinct_cards) {
				int sc = StringUtil.calculate(c.getName(), req);
				if (min == -1 || sc < min) {
					close = c;
					min = sc;
				}
			}
			if (close != null) {
				int id = close.getHighestID();
				rep += "Did you mean: '" + close.getName() + "'?\n\n";
				// rep += close.description();
				this.replyText(apn.getReplyToken(), rep);
				skip = true;
				recursivePushCI(apn, image, GlobalData.getCardInstanceById(id));
			}
		}
		if (!skip)
			this.replyText(apn.getReplyToken(), rep);
	}

	private void case_generate(APNMessageHandler apn) {
		replyText(apn.getReplyToken(), Gen.gen());
	}

	private void case_new(APNMessageHandler apn, boolean image) {
		GlobalData.simple_update(false);
		int skip = 0;
		int number = 5;
		int offset = GlobalData.all_cards.length;
		if (apn.getArgs().length >= 3) {// !(args.length < 2)) {
			if (!apn.isNumber(2)) {
				switch (apn.getArg(2)) {
				case "help": {
					this.replyText(apn.getReplyToken(),
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
					offset = GlobalData.all_cards.length;
					break;
				}
				}
				if (apn.getArgs().length >= 4 && apn.isNumber(3)) {
					number = apn.getNumber(3);// Integer.parseInt(apn.getArg(3));
					if (apn.getArgs().length >= 5 && apn.isNumber(4)) {
						skip = apn.getNumber(4);// Integer.parseInt(apn.getArg(4));
					}
				}
			} else {
				number = Integer.parseInt(apn.getArg(2));
				if (apn.getArgs().length >= 4 && apn.isNumber(3)) {
					skip = apn.getNumber(3);// Integer.parseInt(args[2]);
				}
			}

		}
		if (number > 10)
			number = 10;
		String msg = "";
		ArrayList<Card> printed = new ArrayList<Card>();
		// ArrayList<Message> msgs = new ArrayList<Message>();
		for (int i = 1; i < GlobalData.all_cards.length && number > 0; i++) {
			Card c = GlobalData.all_cards[offset - i];
			if (c != null && c.fusion_level == 2 && !printed.contains(c)
					&& !c.getName().toLowerCase().startsWith("test")
					&& !c.getName().toLowerCase().startsWith("revolt ranger")
					&& !c.getName().toLowerCase().startsWith("cephalodjinn")) {
				printed.add(c);
				if (skip > 0) {
					skip--;
				} else {
					CardInstance ci = GlobalData.getCardInstanceById(c.getHighestID());
					// push(apn.getSenderId(), genCardInstanceMessage(image, ci));
					// this.pushText(apn.getSenderID(), c.description());
					// msg += + "\n---------------------------------------"+"\n";
					/*
					 * for(SkillSpec s : ci.getInfo().skills) { if(s.card_id>0)
					 * push(apn.getSenderId(), genCardInstanceMessage(image,new
					 * CardInstance(s.card_id))); }
					 */
					recursivePushCI(apn, image, ci);
					number--;
				}
			}
		}
		// push(apn.getUserID(), msgs);
		// msg = StringUtil.removeLastCharacter(msg,42);
		// this.replyText(replyToken,msg);
	}

	public void recursivePushCISummon(APNMessageHandler apn, boolean image, CardInstance ci) {
		for (SkillSpec s : ci.getInfo().getSkills()) {
			if (s.getCard_id() > 0)
				recursivePushCI(apn, image, new CardInstance(s.getCard_id()), 1);
		}
	}

	public void recursivePushCI(APNMessageHandler apn, boolean image, CardInstance ci) {
		recursivePushCI(apn, image, ci, 0);
	}

	public void recursivePushCI(APNMessageHandler apn, boolean image, CardInstance ci, int itr) {
		if (image) // only push images
		{
			push(apn.getSenderId(), genCardInstanceMessage(image, ci));
			itr++;
			if (itr < 5) {
				for (SkillSpec s : ci.getInfo().getSkills()) {
					if (s.getCard_id() > 0)
						recursivePushCI(apn, image, new CardInstance(s.getCard_id()), itr);
				}
			}
		}
	}

	private Message genDeckMessage(boolean image, Deck ci) {
		if (image) {
			BufferedImage bi = KitchenSinkApplication.render.renderDeck(ci);
			DownloadedContent d = createTempFile("png");
			try {
				ImageIO.write(bi, "png", d.getPath().toFile());
				Map uploadResult = KitchenSinkApplication.cloudinary.uploader().upload(d.getUri(),
						ObjectUtils.emptyMap());
				Files.deleteIfExists(d.getPath());
				String perm_uri = (String) uploadResult.get("secure_url");
				return new ImageMessage(URI.create(perm_uri), URI.create(perm_uri));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new ImageMessage(URI.create(d.getUri()), URI.create(d.getUri()));
		} else {
			return new TextMessage(ci.toString());
		}
	}

	private ImageMessage cacheImageMessage(BufferedImage bi) {
		DownloadedContent d = createTempFile("png");
		try {
			ImageIO.write(bi, "png", d.getPath().toFile());
			Map uploadResult = KitchenSinkApplication.cloudinary.uploader().upload(d.getUri(), ObjectUtils.emptyMap());
			Files.deleteIfExists(d.getPath());
			String perm_uri = (String) uploadResult.get("secure_url");
			return new ImageMessage(URI.create(perm_uri), URI.create(perm_uri));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ImageMessage(URI.create(d.getUri()), URI.create(d.getUri()));
	}

	private Message genCardInstanceMessage(boolean image, CardInstance ci) {
		if (image) {
			BufferedImage bi = KitchenSinkApplication.render.render(ci);
			return cacheImageMessage(bi);
		} else {
			return new TextMessage(ci.description());
		}
	}

	private Message genCardInstanceTreeMessage(boolean image, CardInstance ci) {
		if (image) {
			BufferedImage bim = KitchenSinkApplication.render.renderTree(ci);
			BufferedImage bi = Render.scaleBilinear(bim, ((double) 1000) / 1150);
			DownloadedContent d = createTempFile("png");
			try {
				ImageIO.write(bi, "png", d.getPath().toFile());
				Map uploadResult = KitchenSinkApplication.cloudinary.uploader().upload(d.getUri(),
						ObjectUtils.emptyMap());
				Files.deleteIfExists(d.getPath());
				String perm_uri = (String) uploadResult.get("secure_url");
				return new ImageMessage(URI.create(perm_uri), URI.create(perm_uri));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return new ImageMessage(URI.create(d.getUri()), URI.create(d.getUri()));
		} else {
			return new TextMessage("Card: " + ci + "\n" + "Fused by: \n[" + StringUtil.removeLastCharacter(GlobalData
					.getInvString(GlobalData.getIDsFromCardInstances(ci.getMaterials())).replaceAll("\n", ", "), 2)
					+ "]\n\n" + "Required Materials (" + ci.getCostFromLowestMaterials() + " SP): \n["
					+ StringUtil.removeLastCharacter(GlobalData
							.getInvString(GlobalData
									.getIDsFromCardInstances(ci.getLowestMaterials().toArray(new CardInstance[] {})))
							.replaceAll("\n", ", "), 2)
					+ "]\n");
		}
	}

	private void case_roadmaps(APNMessageHandler apn) {
		this.reply(apn.getReplyToken(), case_today_next_change(apn));
	}

	private Message case_today_next_change(APNMessageHandler apn) {
		String map = Kong.getRoadMap();
		String rep = "";
		String[] sections = map.split("\\*\\*");
		Date min_next = null;
		Date min_change = null;
		boolean today = apn.equals(1, "today");
		boolean change = apn.equals(1, "change");
		boolean next = apn.equals(1, "next");
		// System.out.println(apn.getMessage() + "->" +today + " " + change + " " +
		// next);
		boolean first = false;
		try {
			for (int i = 3; i < sections.length; i += 2) {
				String title = sections[i];
				String msg = sections[i + 1];
				String[] split = msg.split("\n");
				String date = split[2];
				String conten = split[4];
				for (int j = 5; j < split.length; j++)
					conten += "\n" + split[j];
				conten = conten.trim();
				// rep += title + "\n";
				// rep += date + "\n\n";

				SimpleDateFormat parser = new SimpleDateFormat("yyyyMMMMMMMMM d", Locale.ENGLISH);
				String[] dates = date.split("-");
				String d1 = dates[0];
				d1 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d1.trim(), "(\\d)(st|nd|rd|th)",
						"$1");

				Date dd1 = parser.parse(d1);
				if (change && (min_change == null || dd1.before(min_change))
						&& dd1.after(Calendar.getInstance().getTime())) {
					min_change = dd1;
					rep = title + "\n" + date + "\n\n" + conten;
				}
				if (dates.length > 1) {
					String d2 = dates[1];
					d2 = StringUtil.replaceLast(Calendar.getInstance().get(Calendar.YEAR) + d2.trim(),
							"(\\d)(st|nd|rd|th)", "$1");
					Date dd2 = parser.parse(d2);
					Calendar cal = Calendar.getInstance();// jump one day for borders
					cal.setTime(dd2);
					cal.add(Calendar.DATE, 1);
					dd2 = cal.getTime();
					if (next && (min_next == null || dd1.before(min_next))
							&& dd1.after(Calendar.getInstance().getTime())) {
						min_next = dd1;
						rep = title + "\n" + date + "\n\n" + conten;
					}
					if (today && Calendar.getInstance().getTime().before(dd2)
							&& Calendar.getInstance().getTime().after(dd1) && (!first || (first = false)))
						rep += title + "\n" + date + "\n\n" + conten + "\n\n";
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			rep = "Error during website parse";
		}
		if (rep.equals(""))
			rep = today ? "No event today" : next ? "No next event. Check 'apn today'." : "No known changes";
		return new TextMessage(rep);
		// this.replyText(apn.getReplyToken(), rep);
	}

	private static int getAmazonCoinPrice(String url) {
		String web = Wget.wGet(url);
		if (web == null)
			return -1;
		String[] lines = web.split("\n");
		String cur = "";
		for (String l : lines) {
			cur = l;
			if (cur.matches(".*id=\"priceblock_ourprice\".*"))
				break;
		}
		String m = cur.substring(cur.indexOf("\">"), cur.indexOf("</span>"));
		String value = m.replaceAll(".*(\\d\\d\\d).*", "$1");
		return Integer.parseInt(value);
	}

	private void reddit(APNMessageHandler apn, String tag) {
		String url = getRedditTagUrl(tag);
		this.reply(apn.getReplyToken(), new ImageMessage(URI.create(url), URI.create(url)));
	}

	private void reddit(APNMessageHandler apn) {
		reddit(apn, apn.getArgs().length >= 3 ? apn.getArg(2) : "pics");
	}

	private void art(APNMessageHandler apn) {
		reddit(apn, "Art");
	}

	private void poop(APNMessageHandler apn) {
		if (!apn.getSenderId().equals("Cdf5335b17a5af6f50a33e6a4ee447b4"))
			replyText(apn.getReplyToken(), "Limited to Banter, sorry, not sorry.");
		if (r.nextDouble() < 0.01) {
			reply(apn.getReplyToken(),
					genCardInstanceMessage(true, GlobalData.getCardInstanceByNameAndLevel("Marshal Kylen")));
		}
		{
			reddit(apn, "poop");
		}
	}

	private void chicken(APNMessageHandler apn) {
		reddit(apn, "chickens");
	}

	private void donkey(APNMessageHandler apn) {
		reddit(apn, "donkeys");
	}

	private void pic(APNMessageHandler apn) {
		reddit(apn, "pic");
	}

	private void sticker(String replyToken) {
		int pi = r.nextInt(stickerids.length);
		reply(replyToken, new StickerMessage("" + stickerids[pi][0], "" + stickerids[pi][1]));
	}

	private void gif(APNMessageHandler apn, String tag) {
		Pair<String, String> url = getGIFTagUrl(tag);
		this.reply(apn.getReplyToken(), new VideoMessage(URI.create(url.t), URI.create(url.u)));
	}

	private void gif(APNMessageHandler apn) {
		Pair<String, String> url = null;
		if (apn.getArgs().length >= 3) {
			url = getGIFTagUrl(apn.getArg(2));
			if (url.u == url.t && url.u == null) {
				this.replyText(apn.getReplyToken(), "No gif for '" + apn.getArg(2) + "'");
				return;
			}
		} else {
			url = getGIFUrl();
		}
		this.reply(apn.getReplyToken(), new VideoMessage(URI.create(url.t), URI.create(url.u)));
	}

	private void fail(APNMessageHandler apn) {
		gif(apn, "fail");
	}

	private static String getInsultLong() {
		String msg = Wget.sendGet("https://insult.mattbas.org/api/insult");
		// System.out.println(msg);
		msg = msg.replaceAll("\"", "").replaceAll("&quot;", "\"");
		return msg;
	}

	private static String getInsultShort() {
		String msg = Wget.wGet("http://www.robietherobot.com/insult-generator.htm");
		msg = msg.split("Call them a...")[1].split("<h1>")[1].split("</h1>")[0].trim().replaceAll("\\s+", " ");
		// msg = msg.replaceAll("\"", "").replaceAll("&quot;", "\"");
		return msg;
	}

	private void insult(APNMessageHandler apn) {
		String name = "";
		if (apn.getArgs().length >= 3) {
			name = apn.getFrom(2);
		}
		// System.out.println(msg);
		String insult = "";
		if (r.nextBoolean() == true) {
			insult = getInsultLong();
		} else {
			insult = "You are a " + getInsultShort();
		}
		String msg = "";
		if (name != "")
			msg = insult.replace("You are", name + " is");
		this.replyText(apn.getReplyToken(), msg);
	}

	private void mama(APNMessageHandler apn) {
		String msg = Wget.sendGet("http://api.yomomma.info/");
		JSONObject j = new JSONObject(msg);
		msg = j.getString("joke");
		this.replyText(apn.getReplyToken(), msg);
	}

	private void joke(APNMessageHandler apn) {
		String msg = Wget.sendGet("https://geek-jokes.sameerkumar.website/api");
		// System.out.println(msg);
		msg = msg.replaceAll("\"", "").replaceAll("&quot;", "\"");
		// System.out.println(msg);
		this.replyText(apn.getReplyToken(), msg);
	}

	private void meme(APNMessageHandler apn) {
		String url = getMEMEUrl();
		this.reply(apn.getReplyToken(), new ImageMessage(URI.create(url), URI.create(url)));
	}

	private void xkcd(APNMessageHandler apn) {
		String url = getXKCDUrl();
		this.reply(apn.getReplyToken(), new ImageMessage(URI.create(url), URI.create(url)));
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
		Wget.wGet(img.getPath().toString(), url);
		try {
			Map uploadResult = KitchenSinkApplication.cloudinary.uploader().upload(img.getUri(),
					ObjectUtils.emptyMap());
			Files.deleteIfExists(img.getPath());
			return (String) uploadResult.get("secure_url");
		} catch (Exception e) {
			e.printStackTrace();
		}
		// return "null";
		return img.getUri();
	}

	private String getXKCDUrl() {
		String xkcd = Wget.wGet("https://xkcd.com/" + r.nextInt(2048) + "/");
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

	static String createUri(String path) {
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

	public static DownloadedContent createTempFile(String ext) {
		String fileName = LocalDateTime.now().toString() + '-' + UUID.randomUUID().toString() + '.' + ext;
		Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
		tempFile.toFile().deleteOnExit();
		return new DownloadedContent(tempFile,
				KitchenSinkController.createUri("/downloaded/" + tempFile.getFileName()));
	}

	public static DownloadedContent createTempFolder(String name) {
		String fileName = name;
		Path tempFile = KitchenSinkApplication.downloadedContentDir.resolve(fileName);
		File f = tempFile.toFile();
		f.mkdirs();
		f.deleteOnExit();
		return new DownloadedContent(tempFile, KitchenSinkController.createUri("/downloaded/" + name));
	}

}
