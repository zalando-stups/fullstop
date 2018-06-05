package org.zalando.stups.fullstop.hystrix;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.ApplicationBase;
import org.zalando.stups.clients.kio.CreateOrUpdateApplicationRequest;
import org.zalando.stups.clients.kio.KioOperations;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class HystrixKioOperationsTest {

    private static final String APP_ID = "foo";

    private static final Application APPLICATION = new Application();

    private static final List<ApplicationBase> APPS = newArrayList();

    private static final CreateOrUpdateApplicationRequest CREATE_OR_UPDATE_APP = new CreateOrUpdateApplicationRequest();

    private HystrixKioOperations hystrixKioOperations;

    private KioOperations mockDelegate;

    @Before
    public void setUp() {
        mockDelegate = mock(KioOperations.class);

        hystrixKioOperations = new HystrixKioOperations(mockDelegate);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(mockDelegate);
    }

    @Test
    public void testListApplications() {
        when(mockDelegate.listApplications()).thenReturn(APPS);
        assertThat(hystrixKioOperations.listApplications()).isSameAs(APPS);
        verify(mockDelegate).listApplications();
    }

    @Test
    public void testGetApplicationById() {
        when(mockDelegate.getApplicationById(anyString())).thenReturn(APPLICATION);
        assertThat(hystrixKioOperations.getApplicationById(APP_ID)).isSameAs(APPLICATION);
        verify(mockDelegate).getApplicationById(same(APP_ID));
    }

    @Test
    public void testCreateOrUpdateApplication() {
        hystrixKioOperations.createOrUpdateApplication(CREATE_OR_UPDATE_APP, APP_ID);
        verify(mockDelegate).createOrUpdateApplication(same(CREATE_OR_UPDATE_APP), same(APP_ID));
    }
}
