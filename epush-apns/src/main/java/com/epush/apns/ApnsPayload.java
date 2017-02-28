package com.epush.apns;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.CharArrayWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ApnsPayload
 * 
 * @author guofazhan
 * @version [版本号, 2017/2/28]
 * @see [相关类/方法]
 * @since [产品/模块版本]
 */
public final class ApnsPayload {

	private static final String APS_KEY = "aps";
	private static final String ALERT_KEY = "alert";
	private static final String BADGE_KEY = "badge";
	private static final String SOUND_KEY = "sound";
	private static final String CATEGORY_KEY = "category";
	private static final String CONTENT_AVAILABLE_KEY = "content-available";
	private static final String MUTABLE_CONTENT_KEY = "mutable-content";
	private static final String THREAD_ID_KEY = "thread-id";
	private static final String URL_ARGS_KEY = "url-args";
	private static final String ALERT_TITLE_KEY = "title";
	private static final String ALERT_TITLE_LOC_KEY = "title-loc-key";
	private static final String ALERT_TITLE_ARGS_KEY = "title-loc-args";
	private static final String ALERT_SUBTITLE_KEY = "subtitle";
	private static final String ALERT_SUBTITLE_LOC_KEY = "subtitle-loc-key";
	private static final String ALERT_SUBTITLE_ARGS_KEY = "subtitle-loc-args";
	private static final String ALERT_BODY_KEY = "body";
	private static final String ALERT_LOC_KEY = "loc-key";
	private static final String ALERT_ARGS_KEY = "loc-args";
	private static final String ACTION_KEY = "action";
	private static final String ACTION_LOC_KEY = "action-loc-key";
	private static final String LAUNCH_IMAGE_KEY = "launch-image";

	private final HashMap<String, Object> customProperties = new HashMap<>();

	private static final int DEFAULT_PAYLOAD_SIZE = 4096;

	private static final String ABBREVIATION_SUBSTRING = "…";

	private static final Gson gson = new GsonBuilder().serializeNulls()
			.disableHtmlEscaping().create();

	/**
	 *
	 */
	public static final String DEFAULT_SOUND_FILENAME = "default";

	/**
	 *
	 */
	private String alertBody = null;
	/**
	 *
	 */
	private String localizedAlertKey = null;
	/**
	 *
	 */
	private String[] localizedAlertArguments = null;
	/**
	 *
	 */
	private String alertTitle = null;
	/**
	 *
	 */
	private String localizedAlertTitleKey = null;
	/**
	 *
	 */
	private String[] localizedAlertTitleArguments = null;
	/**
	 *
	 */
	private String alertSubtitle = null;
	/**
	 *
	 */
	private String localizedAlertSubtitleKey = null;
	/**
	 *
	 */
	private String[] localizedAlertSubtitleArguments = null;
	/**
	 *
	 */
	private String launchImageFileName = null;
	/**
	 *
	 */
	private boolean showActionButton = true;
	/**
	 *
	 */
	private String actionButtonLabel = null;
	/**
	 *
	 */
	private String localizedActionButtonKey = null;
	/**
	 *
	 */
	private Integer badgeNumber = null;
	/**
	 *
	 */
	private String soundFileName = null;
	/**
	 *
	 */
	private String categoryName = null;
	/**
	 *
	 */
	private boolean contentAvailable = false;
	/**
	 *
	 */
	private boolean mutableContent = false;
	/**
	 *
	 */
	private String threadId = null;
	/**
	 *
	 */
	private String[] urlArguments = null;

	/**
	 *
	 */
	private boolean preferStringRepresentationForAlerts = false;

	/**
	 *
	 */
	private final CharArrayWriter buffer = new CharArrayWriter(
			DEFAULT_PAYLOAD_SIZE / 4);

	protected ApnsPayload() {
	}

	public ApnsPayload setPreferStringRepresentationForAlerts(
			final boolean preferStringRepresentationForAlerts) {
		this.preferStringRepresentationForAlerts = preferStringRepresentationForAlerts;
		return this;
	}

	public ApnsPayload setAlertBody(final String alertBody) {
		this.alertBody = alertBody;
		this.localizedAlertKey = null;
		this.localizedAlertArguments = null;
		return this;
	}

	public ApnsPayload setLocalizedAlertMessage(final String localizedAlertKey,
			final String... alertArguments) {
		this.localizedAlertKey = localizedAlertKey;
		this.localizedAlertArguments = alertArguments.length > 0
				? alertArguments : null;

		this.alertBody = null;

		return this;
	}

	public ApnsPayload setAlertTitle(final String alertTitle) {
		this.alertTitle = alertTitle;

		this.localizedAlertTitleKey = null;
		this.localizedAlertTitleArguments = null;

		return this;
	}

	public ApnsPayload setLocalizedAlertTitle(
			final String localizedAlertTitleKey,
			final String... alertTitleArguments) {
		this.localizedAlertTitleKey = localizedAlertTitleKey;
		this.localizedAlertTitleArguments = alertTitleArguments.length > 0
				? alertTitleArguments : null;
		this.alertTitle = null;
		return this;
	}

	public ApnsPayload setAlertSubtitle(final String alertSubtitle) {
		this.alertSubtitle = alertSubtitle;

		this.localizedAlertSubtitleKey = null;
		this.localizedAlertSubtitleArguments = null;

		return this;
	}

	public ApnsPayload setLocalizedAlertSubtitle(
			final String localizedAlertSubtitleKey,
			final String... alertSubtitleArguments) {
		this.localizedAlertSubtitleKey = localizedAlertSubtitleKey;
		this.localizedAlertSubtitleArguments = alertSubtitleArguments.length > 0
				? alertSubtitleArguments : null;

		this.alertSubtitle = null;

		return this;
	}

	public ApnsPayload setLaunchImageFileName(
			final String launchImageFilename) {
		this.launchImageFileName = launchImageFilename;
		return this;
	}

	public ApnsPayload setShowActionButton(final boolean showActionButton) {
		this.showActionButton = showActionButton;
		return this;
	}

	public ApnsPayload setActionButtonLabel(final String action) {
		this.actionButtonLabel = action;
		this.localizedActionButtonKey = null;

		return this;
	}

	public ApnsPayload setLocalizedActionButtonKey(
			final String localizedActionButtonKey) {
		this.localizedActionButtonKey = localizedActionButtonKey;
		this.actionButtonLabel = null;

		return this;
	}

	public ApnsPayload setBadgeNumber(final Integer badgeNumber) {
		this.badgeNumber = badgeNumber;
		return this;
	}

	public ApnsPayload setCategoryName(final String categoryName) {
		this.categoryName = categoryName;
		return this;
	}

	public ApnsPayload setSoundFileName(final String soundFileName) {
		this.soundFileName = soundFileName;
		return this;
	}

	public ApnsPayload setContentAvailable(final boolean contentAvailable) {
		this.contentAvailable = contentAvailable;
		return this;
	}

	public ApnsPayload setMutableContent(final boolean mutableContent) {
		this.mutableContent = mutableContent;
		return this;
	}

	public ApnsPayload setThreadId(final String threadId) {
		this.threadId = threadId;
		return this;
	}

	public ApnsPayload setUrlArguments(final List<String> arguments) {
		return this.setUrlArguments(
				arguments != null ? arguments.toArray(new String[0]) : null);
	}

	public ApnsPayload setUrlArguments(final String... arguments) {
		this.urlArguments = arguments;
		return this;
	}

	public ApnsPayload addCustomProperty(final String key, final Object value) {
		this.customProperties.put(key, value);
		return this;
	}

	public String toPayloadStr() {
		return this.toPayloadStr(DEFAULT_PAYLOAD_SIZE);
	}

	public String toPayloadStr(final int maximumPayloadSize) {
		final Map<String, Object> payload = new HashMap<>();

		{
			final Map<String, Object> aps = new HashMap<>();

			if (this.badgeNumber != null) {
				aps.put(BADGE_KEY, this.badgeNumber);
			}

			if (this.soundFileName != null) {
				aps.put(SOUND_KEY, this.soundFileName);
			}

			if (this.categoryName != null) {
				aps.put(CATEGORY_KEY, this.categoryName);
			}

			if (this.contentAvailable) {
				aps.put(CONTENT_AVAILABLE_KEY, 1);
			}

			if (this.mutableContent) {
				aps.put(MUTABLE_CONTENT_KEY, 1);
			}

			if (this.threadId != null) {
				aps.put(THREAD_ID_KEY, this.threadId);
			}

			if (this.urlArguments != null) {
				aps.put(URL_ARGS_KEY, this.urlArguments);
			}

			final Map<String, Object> alert = new HashMap<>();
			{
				if (this.alertBody != null) {
					alert.put(ALERT_BODY_KEY, this.alertBody);
				}

				if (this.alertTitle != null) {
					alert.put(ALERT_TITLE_KEY, this.alertTitle);
				}

				if (this.alertSubtitle != null) {
					alert.put(ALERT_SUBTITLE_KEY, this.alertSubtitle);
				}

				if (this.showActionButton) {
					if (this.localizedActionButtonKey != null) {
						alert.put(ACTION_LOC_KEY,
								this.localizedActionButtonKey);
					}

					if (this.actionButtonLabel != null) {
						alert.put(ACTION_KEY, this.actionButtonLabel);
					}
				} else {
					alert.put(ACTION_LOC_KEY, null);
				}

				if (this.localizedAlertKey != null) {
					alert.put(ALERT_LOC_KEY, this.localizedAlertKey);

					if (this.localizedAlertArguments != null) {
						alert.put(ALERT_ARGS_KEY,
								Arrays.asList(this.localizedAlertArguments));
					}
				}

				if (this.localizedAlertTitleKey != null) {
					alert.put(ALERT_TITLE_LOC_KEY, this.localizedAlertTitleKey);

					if (this.localizedAlertTitleArguments != null) {
						alert.put(ALERT_TITLE_ARGS_KEY, Arrays
								.asList(this.localizedAlertTitleArguments));
					}
				}

				if (this.localizedAlertSubtitleKey != null) {
					alert.put(ALERT_SUBTITLE_LOC_KEY,
							this.localizedAlertSubtitleKey);

					if (this.localizedAlertSubtitleArguments != null) {
						alert.put(ALERT_SUBTITLE_ARGS_KEY, Arrays
								.asList(this.localizedAlertSubtitleArguments));
					}
				}

				if (this.launchImageFileName != null) {
					alert.put(LAUNCH_IMAGE_KEY, this.launchImageFileName);
				}
			}

			if (alert.size() == 1 && alert.containsKey(ALERT_BODY_KEY)
					&& this.preferStringRepresentationForAlerts) {
				aps.put(ALERT_KEY, alert.get(ALERT_BODY_KEY));
			} else if (!alert.isEmpty()) {
				aps.put(ALERT_KEY, alert);
			}

			payload.put(APS_KEY, aps);
		}

		for (final Map.Entry<String, Object> entry : this.customProperties
				.entrySet()) {
			payload.put(entry.getKey(), entry.getValue());
		}

		this.buffer.reset();
		gson.toJson(payload, this.buffer);

		final String payloadString = this.buffer.toString();
		final int initialPayloadSize = payloadString
				.getBytes(StandardCharsets.UTF_8).length;

		final String fittedPayloadString;

		if (initialPayloadSize <= maximumPayloadSize) {
			fittedPayloadString = payloadString;
		} else {
			if (this.alertBody != null) {
				this.replaceMessageBody(payload, "");

				this.buffer.reset();
				gson.toJson(payload, this.buffer);

				final int payloadSizeWithEmptyMessage = this.buffer.toString()
						.getBytes(StandardCharsets.UTF_8).length;

				if (payloadSizeWithEmptyMessage >= maximumPayloadSize) {
					throw new IllegalArgumentException(
							"Payload exceeds maximum size even with an empty message body.");
				}

				final int maximumEscapedMessageBodySize = maximumPayloadSize
						- payloadSizeWithEmptyMessage - ABBREVIATION_SUBSTRING
								.getBytes(StandardCharsets.UTF_8).length;

				final String fittedMessageBody = this.alertBody.substring(0,
						ApnsPayload.getLengthOfJsonEscapedUtf8StringFittingSize(
								this.alertBody, maximumEscapedMessageBodySize));

				this.replaceMessageBody(payload,
						fittedMessageBody + ABBREVIATION_SUBSTRING);

				this.buffer.reset();
				gson.toJson(payload, this.buffer);

				fittedPayloadString = this.buffer.toString();
			} else {
				throw new IllegalArgumentException(String.format(
						"Payload size is %d bytes (with a maximum of %d bytes) and cannot be shortened.",
						initialPayloadSize, maximumPayloadSize));
			}
		}

		return fittedPayloadString;
	}

	private void replaceMessageBody(final Map<String, Object> payload,
			final String messageBody) {
		@SuppressWarnings("unchecked")
		final Map<String, Object> aps = (Map<String, Object>) payload
				.get(APS_KEY);
		final Object alert = aps.get(ALERT_KEY);

		if (alert != null) {
			if (alert instanceof String) {
				aps.put(ALERT_KEY, messageBody);
			} else {
				@SuppressWarnings("unchecked")
				final Map<String, Object> alertObject = (Map<String, Object>) alert;

				if (alertObject.get(ALERT_BODY_KEY) != null) {
					alertObject.put(ALERT_BODY_KEY, messageBody);
				} else {
					throw new IllegalArgumentException(
							"Payload has no message body.");
				}
			}
		} else {
			throw new IllegalArgumentException("Payload has no message body.");
		}
	}

	static int getLengthOfJsonEscapedUtf8StringFittingSize(final String string,
			final int maximumSize) {
		int i = 0;
		int cumulativeSize = 0;

		for (i = 0; i < string.length(); i++) {
			final char c = string.charAt(i);
			final int charSize = getSizeOfJsonEscapedUtf8Character(c);

			if (cumulativeSize + charSize > maximumSize) {
				break;
			}

			cumulativeSize += charSize;

			if (Character.isHighSurrogate(c)) {
				i++;
			}
		}

		return i;
	}

	static int getSizeOfJsonEscapedUtf8Character(final char c) {
		final int charSize;

		if (c == '"' || c == '\\' || c == '\b' || c == '\f' || c == '\n'
				|| c == '\r' || c == '\t') {
			// Character is backslash-escaped in JSON
			charSize = 2;
		} else if (c <= 0x001F || c == '\u2028' || c == '\u2029') {
			// Character will be represented as an escaped control character
			charSize = 6;
		} else {
			// The character will be represented as an un-escaped UTF8 character
			if (c <= 0x007F) {
				charSize = 1;
			} else if (c <= 0x07FF) {
				charSize = 2;
			} else if (Character.isHighSurrogate(c)) {
				charSize = 4;
			} else {
				charSize = 3;
			}
		}

		return charSize;
	}

}
