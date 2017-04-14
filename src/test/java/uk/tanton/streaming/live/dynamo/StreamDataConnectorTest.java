package uk.tanton.streaming.live.dynamo;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.tanton.streaming.live.dynamo.domain.Publisher;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamDataConnectorTest {

    private StreamDataConnector underTest;

    @Mock private DynamoDBMapper mapper;

    @Before
    public void setup() {
        underTest = new StreamDataConnector(mapper, new DynamoTableConfig("accounts-table", "publishers-table", "streams-table"));
    }

    @Test
    public void it() {
        final Publisher expectedPublisher = new Publisher("accountId", "passwordHash", "passwordSalt", "username", new Date(), new Date());
        when(mapper.load(eq(Publisher.class), eq("username"), anyObject())).thenReturn(expectedPublisher);
//        when(mapper.getItem(getItemRequestArgumentCaptor.capture())).thenReturn(null);

        final Publisher actualPublisher = underTest.getPublisher("username");


        final ArgumentCaptor<DynamoDBMapperConfig> configCaptor = ArgumentCaptor.forClass(DynamoDBMapperConfig.class);
        verify(mapper).load(eq(Publisher.class), eq("username"), configCaptor.capture());

        assertEquals("publishers-table", configCaptor.getValue().getTableNameOverride().getTableName());
        assertEquals(expectedPublisher, actualPublisher);

        verifyNoMoreInteractions(mapper);
    }

}