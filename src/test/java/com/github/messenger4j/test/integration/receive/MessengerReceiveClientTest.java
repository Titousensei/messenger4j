package com.github.messenger4j.test.integration.receive;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.github.messenger4j.exceptions.MessengerVerificationException;
import com.github.messenger4j.v3.Messenger;
import com.github.messenger4j.v3.receive.AccountLinkingEvent;
import com.github.messenger4j.v3.receive.Attachment;
import com.github.messenger4j.v3.receive.AttachmentMessageEvent;
import com.github.messenger4j.v3.receive.Event;
import com.github.messenger4j.v3.receive.MessageDeliveredEvent;
import com.github.messenger4j.v3.receive.MessageEchoEvent;
import com.github.messenger4j.v3.receive.MessageReadEvent;
import com.github.messenger4j.v3.receive.OptInEvent;
import com.github.messenger4j.v3.receive.PostbackEvent;
import com.github.messenger4j.v3.receive.QuickReplyMessageEvent;
import com.github.messenger4j.v3.receive.Referral;
import com.github.messenger4j.v3.receive.RichMediaAttachment;
import com.github.messenger4j.v3.receive.TextMessageEvent;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * @author Max Grabenhorst
 * @since 0.6.0
 */
public class MessengerReceiveClientTest {

    @SuppressWarnings("unchecked")
    private Consumer<Event> mockEventHandler = (Consumer<Event>) mock(Consumer.class);

    private final Messenger messenger = Messenger.create("test", "60efff025951cddde78c8d03de52cc90", "CUSTOM_VERIFY_TOKEN");

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfObjectTypeIsNotPage() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"testValue\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1458692752478,\n" +
                "            \"message\": {\n" +
                "                \"mid\": \"mid.1457764197618:41d102a3e1ae206a38\",\n" +
                "                \"text\": \"hello, world!\",\n" +
                "                \"quick_reply\": {\n" +
                "                    \"payload\": \"DEVELOPER_DEFINED_PAYLOAD\"\n" +
                "                }\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then - throw exception
    }

    @Test
    public void shouldHandleAttachmentMessageEvent() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1458692752478,\n" +
                "            \"message\": {\n" +
                "                \"mid\": \"mid.1458696618141:b4ef9d19ec21086067\",\n" +
                "                \"attachments\": [{\n" +
                "                    \"type\": \"image\",\n" +
                "                    \"payload\": {\n" +
                "                        \"url\": \"IMAGE_URL\"\n" +
                "                    }\n" +
                "                }, {\n" +
                "                    \"type\": \"location\",\n" +
                "                    \"payload\": {\n" +
                "                        \"coordinates\": {\n" +
                "                            \"lat\": 52.3765533,\n" +
                "                            \"long\": 9.7389123\n" +
                "                        }\n" +
                "                    }\n" +
                "                }]\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final AttachmentMessageEvent attachmentMessageEvent = event.asAttachmentMessageEvent();
        assertThat(attachmentMessageEvent.senderId(), equalTo("USER_ID"));
        assertThat(attachmentMessageEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(attachmentMessageEvent.timestamp(), equalTo(Instant.ofEpochMilli(1458692752478L)));
        assertThat(attachmentMessageEvent.messageId(), equalTo("mid.1458696618141:b4ef9d19ec21086067"));
        assertThat(attachmentMessageEvent.attachments(), hasSize(2));

        final Attachment firstAttachment = attachmentMessageEvent.attachments().get(0);
        assertThat(firstAttachment.isRichMediaAttachment(), is(true));
        assertThat(firstAttachment.asRichMediaAttachment().type(), equalTo(RichMediaAttachment.Type.IMAGE));
        assertThat(firstAttachment.asRichMediaAttachment().url(), equalTo("IMAGE_URL"));

        final Attachment secondAttachment = attachmentMessageEvent.attachments().get(1);
        assertThat(secondAttachment.isLocationAttachment(), is(true));
        assertThat(secondAttachment.asLocationAttachment().latitude(), equalTo(52.3765533));
        assertThat(secondAttachment.asLocationAttachment().longitude(), equalTo(9.7389123));
    }

    @Test
    public void shouldHandleOptInEvent() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1234567890,\n" +
                "            \"optin\": {\n" +
                "                \"ref\": \"PASS_THROUGH_PARAM\"\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final OptInEvent optInEvent = event.asOptInEvent();
        assertThat(optInEvent.senderId(), equalTo("USER_ID"));
        assertThat(optInEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(optInEvent.timestamp(), equalTo(Instant.ofEpochMilli(1234567890L)));
        assertThat(optInEvent.refPayload(), equalTo(Optional.of("PASS_THROUGH_PARAM")));
    }

    @Test
    public void shouldHandleTextEchoMessageEvent() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1480114700424,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1480114700296,\n" +
                "            \"message\": {\n" +
                "                \"is_echo\": true,\n" +
                "                \"app_id\": 1517776481860111,\n" +
                "                \"metadata\": \"DEVELOPER_DEFINED_METADATA_STRING\",\n" +
                "                \"mid\": \"mid.1457764197618:41d102a3e1ae206a38\",\n" +
                "                \"seq\": 282,\n" +
                "                \"text\": \"hello, text message world!\"\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final MessageEchoEvent messageEchoEvent = event.asMessageEchoEvent();
        assertThat(messageEchoEvent.senderId(), equalTo("PAGE_ID"));
        assertThat(messageEchoEvent.recipientId(), equalTo("USER_ID"));
        assertThat(messageEchoEvent.timestamp(), equalTo(Instant.ofEpochMilli(1480114700296L)));
        assertThat(messageEchoEvent.messageId(), equalTo("mid.1457764197618:41d102a3e1ae206a38"));
        assertThat(messageEchoEvent.appId(), equalTo("1517776481860111"));
        assertThat(messageEchoEvent.metadata(), equalTo(Optional.of("DEVELOPER_DEFINED_METADATA_STRING")));
    }

    @Test
    public void shouldHandleTemplateEchoMessageEvent() throws Exception {
        //given
        final String payload = "{\"object\":\"page\",\"entry\":[{\"id\":\"171999997131834678\",\"time\":1480120722215," +
                "\"messaging\":[{\"sender\":{\"id\":\"17175299999834678\"},\"recipient\":{\"id\":\"1256299999730577\"}," +
                "\"timestamp\":1480120402725,\"message\":{\"is_echo\":true,\"app_id\":1559999994822905," +
                "\"mid\":\"mid.1480199999925:83392d9f65\",\"seq\":294,\"attachments\":[{\"title\":\"Samsung Gear VR, " +
                "Oculus Rift\",\"url\":null,\"type\":\"template\",\"payload\":{\"template_type\":\"receipt\"," +
                "\"recipient_name\":\"Peter Chang\",\"order_number\":\"order-505.0\",\"currency\":\"USD\"," +
                "\"timestamp\":1428444852,\"payment_method\":\"Visa 1234\",\"summary\":{\"total_cost\":626.66," +
                "\"total_tax\":57.67,\"subtotal\":698.99,\"shipping_cost\":20}," +
                "\"address\":{\"city\":\"Menlo Park\",\"country\":\"US\",\"postal_code\":\"94025\",\"state\":\"CA\"," +
                "\"street_1\":\"1 Hacker Way\",\"street_2\":\"\"},\"elements\":[{\"title\":\"Samsung Gear VR\"," +
                "\"quantity\":1,\"image_url\":" +
                "\"https:\\/\\/raw.githubusercontent.com\\/fbsamples\\/messenger-platform-samples\\/master\\/node\\" +
                "/public\\/assets\\/gearvrsq.png\",\"price\":99.99,\"subtitle\":\"Frost White\"},{\"title\":" +
                "\"Oculus Rift\",\"quantity\":1,\"image_url\":\"https:\\/\\/raw.githubusercontent.com\\/fbsamples\\" +
                "/messenger-platform-samples\\/master\\/node\\/public\\/assets\\/riftsq.png\",\"price\":599," +
                "\"subtitle\":\"Includes: headset, sensor, remote\"}],\"adjustments\":[{\"name\":\"New Customer Discount\"," +
                "\"amount\":-50},{\"name\":\"$100 Off Coupon\",\"amount\":-100}]}}]}}]}]}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final MessageEchoEvent messageEchoEvent = event.asMessageEchoEvent();
        assertThat(messageEchoEvent.senderId(), equalTo("17175299999834678"));
        assertThat(messageEchoEvent.recipientId(), equalTo("1256299999730577"));
        assertThat(messageEchoEvent.timestamp(), equalTo(Instant.ofEpochMilli(1480120402725L)));
        assertThat(messageEchoEvent.messageId(), equalTo("mid.1480199999925:83392d9f65"));
        assertThat(messageEchoEvent.appId(), equalTo("1559999994822905"));
        assertThat(messageEchoEvent.metadata(), is(Optional.empty()));
    }

    @Test
    public void shouldHandleQuickReplyMessageEvent() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1458692752478,\n" +
                "            \"message\": {\n" +
                "                \"mid\": \"mid.1457764197618:41d102a3e1ae206a38\",\n" +
                "                \"text\": \"hello, world!\",\n" +
                "                \"quick_reply\": {\n" +
                "                    \"payload\": \"DEVELOPER_DEFINED_PAYLOAD\"\n" +
                "                }\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final QuickReplyMessageEvent quickReplyMessageEvent = event.asQuickReplyMessageEvent();
        assertThat(quickReplyMessageEvent.senderId(), equalTo("USER_ID"));
        assertThat(quickReplyMessageEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(quickReplyMessageEvent.timestamp(), equalTo(Instant.ofEpochMilli(1458692752478L)));
        assertThat(quickReplyMessageEvent.messageId(), equalTo("mid.1457764197618:41d102a3e1ae206a38"));
        assertThat(quickReplyMessageEvent.text(), equalTo("hello, world!"));
        assertThat(quickReplyMessageEvent.payload(), equalTo("DEVELOPER_DEFINED_PAYLOAD"));
    }

    @Test
    public void shouldHandleTextMessageEvent() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1458692752478,\n" +
                "            \"message\": {\n" +
                "                \"mid\": \"mid.1457764197618:41d102a3e1ae206a38\",\n" +
                "                \"text\": \"hello, text message world!\"\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final TextMessageEvent textMessageEvent = event.asTextMessageEvent();
        assertThat(textMessageEvent.senderId(), equalTo("USER_ID"));
        assertThat(textMessageEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(textMessageEvent.timestamp(), equalTo(Instant.ofEpochMilli(1458692752478L)));
        assertThat(textMessageEvent.messageId(), equalTo("mid.1457764197618:41d102a3e1ae206a38"));
        assertThat(textMessageEvent.text(), equalTo("hello, text message world!"));
    }

    @Test
    public void shouldHandlePostbackEvent() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "  \"sender\":{\n" +
                "    \"id\":\"<PSID>\"\n" +
                "  },\n" +
                "  \"recipient\":{\n" +
                "    \"id\":\"<PAGE_ID>\"\n" +
                "  },\n" +
                "  \"timestamp\":1458692752478,\n" +
                "  \"postback\":{\n" +
                "    \"title\": \"<TITLE_FOR_THE_CTA>\",  \n" +
                "    \"payload\": \"<USER_DEFINED_PAYLOAD>\",\n" +
                "    \"referral\": {\n" +
                "      \"ref\": \"<USER_DEFINED_REFERRAL_PARAM>\",\n" +
                "      \"source\": \"<SHORTLINK>\",\n" +
                "      \"type\": \"OPEN_THREAD\"\n" +
                "    }\n" +
                "  }\n" +
                "}]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final PostbackEvent postbackEvent = event.asPostbackEvent();
        assertThat(postbackEvent.senderId(), equalTo("<PSID>"));
        assertThat(postbackEvent.recipientId(), equalTo("<PAGE_ID>"));
        assertThat(postbackEvent.timestamp(), equalTo(Instant.ofEpochMilli(1458692752478L)));
        assertThat(postbackEvent.title(), equalTo("<TITLE_FOR_THE_CTA>"));
        assertThat(postbackEvent.payload(), equalTo(Optional.of("<USER_DEFINED_PAYLOAD>")));
        assertThat(postbackEvent.referral(), equalTo(Optional.of(new Referral("<SHORTLINK>",
                "OPEN_THREAD", "<USER_DEFINED_REFERRAL_PARAM>", null))));
    }

    @Test
    public void shouldHandleAccountLinkingEventWithStatusLinked() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1234567890,\n" +
                "            \"account_linking\": {\n" +
                "                \"status\": \"linked\",\n" +
                "                \"authorization_code\": \"PASS_THROUGH_AUTHORIZATION_CODE\"\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final AccountLinkingEvent accountLinkingEvent = event.asAccountLinkingEvent();
        assertThat(accountLinkingEvent.senderId(), equalTo("USER_ID"));
        assertThat(accountLinkingEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(accountLinkingEvent.timestamp(), equalTo(Instant.ofEpochMilli(1234567890L)));
        assertThat(accountLinkingEvent.status(), equalTo(AccountLinkingEvent.Status.LINKED));
        assertThat(accountLinkingEvent.authorizationCode(), equalTo(Optional.of("PASS_THROUGH_AUTHORIZATION_CODE")));
    }

    @Test
    public void shouldHandleAccountLinkingEventWithStatusUnlinked() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1234567890,\n" +
                "            \"account_linking\": {\n" +
                "                \"status\": \"unlinked\"\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final AccountLinkingEvent accountLinkingEvent = event.asAccountLinkingEvent();
        assertThat(accountLinkingEvent.senderId(), equalTo("USER_ID"));
        assertThat(accountLinkingEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(accountLinkingEvent.timestamp(), equalTo(Instant.ofEpochMilli(1234567890L)));
        assertThat(accountLinkingEvent.status(), equalTo(AccountLinkingEvent.Status.UNLINKED));
        assertThat(accountLinkingEvent.authorizationCode(), is(Optional.empty()));
    }

    @Test
    public void shouldHandleMessageReadEvent() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1458668856463,\n" +
                "            \"read\": {\n" +
                "                \"watermark\": 1458668856253\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final MessageReadEvent messageReadEvent = event.asMessageReadEvent();
        assertThat(messageReadEvent.senderId(), equalTo("USER_ID"));
        assertThat(messageReadEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(messageReadEvent.timestamp(), equalTo(Instant.ofEpochMilli(1458668856463L)));
        assertThat(messageReadEvent.watermark(), equalTo(Instant.ofEpochMilli(1458668856253L)));
    }

    @Test
    public void shouldHandleMessageDeliveredEventWithMids() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"delivery\": {\n" +
                "                \"mids\": [\n" +
                "                    \"mid.1458668856218:ed81099e15d3f4f233\"\n" +
                "                ],\n" +
                "                \"watermark\": 1458668856253\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final MessageDeliveredEvent messageDeliveredEvent = event.asMessageDeliveredEvent();
        assertThat(messageDeliveredEvent.senderId(), equalTo("USER_ID"));
        assertThat(messageDeliveredEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(messageDeliveredEvent.watermark(), equalTo(Instant.ofEpochMilli(1458668856253L)));
        assertThat(messageDeliveredEvent.messageIds().get(), hasSize(1));
        assertThat(messageDeliveredEvent.messageIds().get().get(0), equalTo("mid.1458668856218:ed81099e15d3f4f233"));
    }

    @Test
    public void shouldHandleMessageDeliveredEventWithoutMids() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"delivery\": {\n" +
                "                \"watermark\": 1458668856253\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        final MessageDeliveredEvent messageDeliveredEvent = event.asMessageDeliveredEvent();
        assertThat(messageDeliveredEvent.senderId(), equalTo("USER_ID"));
        assertThat(messageDeliveredEvent.recipientId(), equalTo("PAGE_ID"));
        assertThat(messageDeliveredEvent.watermark(), equalTo(Instant.ofEpochMilli(1458668856253L)));
        assertThat(messageDeliveredEvent.messageIds().isPresent(), is(false));
    }

    @Test
    public void shouldHandleEventTypeThatIsUnsupported() throws Exception {
        //given
        final String payload = "{\n" +
                "    \"object\": \"page\",\n" +
                "    \"entry\": [{\n" +
                "        \"id\": \"PAGE_ID\",\n" +
                "        \"time\": 1458692752478,\n" +
                "        \"messaging\": [{\n" +
                "            \"sender\": {\n" +
                "                \"id\": \"USER_ID\"\n" +
                "            },\n" +
                "            \"recipient\": {\n" +
                "                \"id\": \"PAGE_ID\"\n" +
                "            },\n" +
                "            \"timestamp\": 1458692752478,\n" +
                "            \"EVENT_TYPE_THAT_IS_UNSUPPORTED\": {\n" +
                "                \"mid\": \"mid.1457764197618:41d102a3e1ae206a38\"\n" +
                "            }\n" +
                "        }]\n" +
                "    }]\n" +
                "}";

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        assertThat(event.senderId(), equalTo("USER_ID"));
        assertThat(event.recipientId(), equalTo("PAGE_ID"));
        assertThat(event.timestamp(), equalTo(Instant.ofEpochMilli(1458692752478L)));

        assertThat(event.isAccountLinkingEvent(), is(false));
        assertThat(event.isMessageDeliveredEvent(), is(false));
        assertThat(event.isMessageEchoEvent(), is(false));
        assertThat(event.isMessageReadEvent(), is(false));
        assertThat(event.isOptInEvent(), is(false));
        assertThat(event.isPostbackEvent(), is(false));
        assertThat(event.isReferralEvent(), is(false));
        assertThat(event.isAttachmentMessageEvent(), is(false));
        assertThat(event.isQuickReplyMessageEvent(), is(false));
        assertThat(event.isTextMessageEvent(), is(false));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfNoPayloadProvided() throws Exception {
        //given
        final String payload = null;

        //when
        messenger.onReceiveEvents(payload, null, mockEventHandler);

        //then - throw exception
    }

    @Test
    public void shouldVerifyTheGivenSignature() throws Exception {
        //given
        final String payload = "{\"object\":\"page\",\"entry\":[{\"id\":\"1717527131834678\",\"time\":1475942721780," +
                "\"messaging\":[{\"sender\":{\"id\":\"1256217357730577\"},\"recipient\":{\"id\":\"1717527131834678\"}," +
                "\"timestamp\":1475942721741,\"message\":{\"mid\":\"mid.1475942721728:3b9e3646712f9bed52\"," +
                "\"seq\":123,\"text\":\"34wrr3wr\"}}]}]}";
        final String signature = "sha1=3daa41999293ff66c3eb313e04bcf77861bb0276";

        //when
        messenger.onReceiveEvents(payload, signature, mockEventHandler);

        //then
        final ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventHandler).accept(eventCaptor.capture());
        final Event event = eventCaptor.getValue();

        assertThat(event.asTextMessageEvent().text(), is(equalTo("34wrr3wr")));
    }

    @Test(expected = MessengerVerificationException.class)
    public void shouldThrowExceptionIfSignatureIsInvalid() throws Exception {
        //given
        final String payload = "{\"object\":\"page\",\"entry\":[{\"id\":\"1717527131834678\",\"time\":1475942721780," +
                "\"messaging\":[{\"sender\":{\"id\":\"1256217357730577\"},\"recipient\":{\"id\":\"1717527131834678\"}," +
                "\"timestamp\":1475942721741,\"message\":{\"mid\":\"mid.1475942721728:3b9e3646712f9bed52\"," +
                "\"seq\":123,\"text\":\"CHANGED_TEXT_SO_SIGNATURE_IS_INVALID\"}}]}]}";
        final String signature = "sha1=3daa41999293ff66c3eb313e04bcf77861bb0276";

        //when
        messenger.onReceiveEvents(payload, signature, mockEventHandler);

        //then - throw exception
    }

    @Test
    public void shouldVerifyTheWebhook() throws Exception {
        //given
        final String mode = "subscribe";
        final String verifyToken = "CUSTOM_VERIFY_TOKEN";

        //when
        messenger.verifyWebhook(mode, verifyToken);

        //then - no exception is thrown
    }

    @Test(expected = MessengerVerificationException.class)
    public void shouldThrowExceptionIfVerifyModeIsInvalid() throws Exception {
        //given
        final String mode = "INVALID_MODE";
        final String verifyToken = "CUSTOM_VERIFY_TOKEN";

        //when
        messenger.verifyWebhook(mode, verifyToken);

        //then - throw exception
    }

    @Test(expected = MessengerVerificationException.class)
    public void shouldThrowExceptionIfVerifyTokenIsInvalid() throws Exception {
        //given
        final String mode = "subscribe";
        final String verifyToken = "INVALID_VERIFY_TOKEN";

        //when
        messenger.verifyWebhook(mode, verifyToken);

        //then - throw exception
    }
}