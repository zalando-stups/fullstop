package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.zalando.stups.fullstop.aws.ClientProvider;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.fromName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class TaupageExpirationTimeProviderImplTest {

    private static final String REGION_NAME = "eu-central-1";
    private static final String IMAGE_OWNER = "111222333444";
    private static final String IMAGE_ID = "ami-123abc";

    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ClientProvider mockClientProvider;
    @Mock
    private AmazonEC2Client mockEC2Client;

    private TaupageExpirationTimeProviderImpl expirationTimeProvider;


    @Before
    public void setUp() {
        expirationTimeProvider = new TaupageExpirationTimeProviderImpl(mockClientProvider);
        when(mockClientProvider.getClient(eq(AmazonEC2Client.class), anyString(), any())).thenReturn(mockEC2Client);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockClientProvider, mockEC2Client);
    }

    @Test
    public void getExpirationTime() {
        final DescribeTagsResult response = new DescribeTagsResult()
                .withTags(new TagDescription()
                        .withResourceType("image")
                        .withResourceId(IMAGE_ID)
                        .withKey(TaupageExpirationTimeProviderImpl.TAG_KEY)
                        .withValue("2018-06-20T03:00:00+02:00"));

        when(mockEC2Client.describeTags(any(DescribeTagsRequest.class))).thenReturn(response);

        final ZonedDateTime result = expirationTimeProvider.getExpirationTime(REGION_NAME, IMAGE_OWNER, IMAGE_ID);
        assertThat(result).isEqualTo(ZonedDateTime.of(2018, 6, 20, 3, 0, 0, 0, ZoneOffset.ofHours(2)));

        verify(mockClientProvider).getClient(eq(AmazonEC2Client.class), eq(IMAGE_OWNER), eq(getRegion(fromName(REGION_NAME))));
        verify(mockEC2Client).describeTags(
                eq(new DescribeTagsRequest().withFilters(
                        new Filter("resource-id").withValues(IMAGE_ID),
                        new Filter("resource-type").withValues("image"),
                        new Filter("key").withValues(TaupageExpirationTimeProviderImpl.TAG_KEY))));
    }

    @Test
    public void getAbsentExpirationTime() {
        when(mockEC2Client.describeTags(any(DescribeTagsRequest.class))).thenReturn(new DescribeTagsResult());

        final ZonedDateTime result = expirationTimeProvider.getExpirationTime(REGION_NAME, IMAGE_OWNER, IMAGE_ID);
        assertThat(result).isNull();

        verify(mockClientProvider).getClient(eq(AmazonEC2Client.class), eq(IMAGE_OWNER), eq(getRegion(fromName(REGION_NAME))));
        verify(mockEC2Client).describeTags(
                eq(new DescribeTagsRequest().withFilters(
                        new Filter("resource-id").withValues(IMAGE_ID),
                        new Filter("resource-type").withValues("image"),
                        new Filter("key").withValues(TaupageExpirationTimeProviderImpl.TAG_KEY))));
    }
}
