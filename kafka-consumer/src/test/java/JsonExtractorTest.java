import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JsonExtractorTest {

    @Test
    public void testExtract() {
        String json =
            "{\"date\":{\"string\":\"2017-03-20\"},\"time\":{\"string\":\"20:04:13:563\"},"
                + "\"event_nr\":1572470,\"interface\":\"Transaction Manager\",\"event_id\":5001,"
                + "\"date_time\":1490040253563,\"entity\":\"Transaction Manager\",\"state\":0,"
                + "\"msg_param_1\":{\"string\":\"ISWSnk\"},\"msg_param_2\":{\"string\":\"Application startup\"},"
                + "\"msg_param_3\":null,\"msg_param_4\":null,\"msg_param_5\":null,\"msg_param_6\":null,"
                + "\"msg_param_7\":null,\"msg_param_8\":null,\"msg_param_9\":null,\"long_msg_param_1\":null,"
                + "\"long_msg_param_2\":null,\"long_msg_param_3\":null,\"long_msg_param_4\":null,"
                + "\"long_msg_param_5\":null,\"long_msg_param_6\":null,\"long_msg_param_7\":null,"
                + "\"long_msg_param_8\":null,\"long_msg_param_9\":null,\"last_sent\":{\"long\":1490040253563},"
                + "\"transmit_count\":{\"int\":1},\"team_id\":null,\"app_id\":{\"int\":4},"
                + "\"logged_by_app_id\":{\"int\":4},\"entity_type\":{\"int\":3},\"binary_data\":null}";
        assertThat("Transaction Manager", equalTo(JsonExtractor.returnJsonValue(json, "entity")));
    }

    @Test
    public void stringMinimizeTest() {
        assertThat("AHHHAAAAAARTFUHLAAAAAHV".replaceAll("A{4}(?!A)", "$0"),
            equalTo("AHHHAAAAAARTFUHLAAAAAHV"));
        assertThat("AHHHAAAAAARTFUHLAAAAAHV".replaceAll("A{4}", ""), equalTo("AHHHAARTFUHLAHV"));
    }
}