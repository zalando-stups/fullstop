package org.zalando.stups.fullstop.plugin.provider.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.KioApplicationProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KioApplicationProviderImplTest {

    private static final String INSTANCE_ID = "i-1234";
    private KioApplicationProvider kioApplicationProvider;
    private EC2InstanceContext ec2InstanceContextMock;
    private KioOperations kioOperationsMock;


    @Before
    public void setUp() {

        kioOperationsMock = mock(KioOperations.class);
        kioApplicationProvider = new KioApplicationProviderImpl(kioOperationsMock);

        ec2InstanceContextMock = mock(EC2InstanceContext.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock, kioOperationsMock);
    }

    @Test
    public void testApplicationFound() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));
        when(kioOperationsMock.getApplicationById(eq(INSTANCE_ID))).thenReturn(new Application());

        Optional<Application> result = kioApplicationProvider.apply(ec2InstanceContextMock);
        assertThat(result).isPresent();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(kioOperationsMock).getApplicationById(eq(INSTANCE_ID));
    }

    @Test
    public void testApplicationNotFoundInKio() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));
        when(kioOperationsMock.getApplicationById(eq(INSTANCE_ID))).thenReturn(null);

        Optional<Application> result = kioApplicationProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(kioOperationsMock).getApplicationById(eq(INSTANCE_ID));
    }

    @Test
    public void testApplicationNotFound() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.empty());

        Optional<Application> result = kioApplicationProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
    }
}