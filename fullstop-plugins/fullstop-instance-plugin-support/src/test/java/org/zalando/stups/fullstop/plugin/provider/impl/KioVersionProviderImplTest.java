package org.zalando.stups.fullstop.plugin.provider.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.KioVersionProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class KioVersionProviderImplTest {

    private static final String INSTANCE_ID = "i-1234";
    public static final String VERSION_ID = "1.0.2-234";
    private KioVersionProvider kioVersionProvider;
    private EC2InstanceContext ec2InstanceContextMock;
    private KioOperations kioOperationsMock;

    @Before
    public void setUp() {

        kioOperationsMock = mock(KioOperations.class);
        kioVersionProvider = new KioVersionProviderImpl(kioOperationsMock);

        ec2InstanceContextMock = mock(EC2InstanceContext.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock, kioOperationsMock);
    }

    @Test
    public void testVersionFound() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));
        when(ec2InstanceContextMock.getVersionId()).thenReturn(Optional.of(VERSION_ID));
        when(kioOperationsMock.getApplicationVersion(eq(INSTANCE_ID),eq(VERSION_ID))).thenReturn(new Version());

        Optional<Version> result = kioVersionProvider.apply(ec2InstanceContextMock);
        assertThat(result).isPresent();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getVersionId();
        verify(kioOperationsMock).getApplicationVersion(eq(INSTANCE_ID),eq(VERSION_ID));
    }

    @Test
    public void testVersionNotFound1() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.empty());
        when(ec2InstanceContextMock.getVersionId()).thenReturn(Optional.of(VERSION_ID));

        Optional<Version> result = kioVersionProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getVersionId();
    }

    @Test
    public void testVersionNotFound2() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));
        when(ec2InstanceContextMock.getVersionId()).thenReturn(Optional.empty());

        Optional<Version> result = kioVersionProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getVersionId();
    }

    @Test
    public void testApplicationIdBlank() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(" "));
        when(ec2InstanceContextMock.getVersionId()).thenReturn(Optional.of(VERSION_ID));

        Optional<Version> result = kioVersionProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getVersionId();
    }

    @Test
    public void testVersionIdBlank() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));
        when(ec2InstanceContextMock.getVersionId()).thenReturn(Optional.of(" "));

        Optional<Version> result = kioVersionProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getVersionId();
    }

    @Test
    public void testNotFoundException() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));
        when(ec2InstanceContextMock.getVersionId()).thenReturn(Optional.of(VERSION_ID));
        when(kioOperationsMock.getApplicationVersion(eq(INSTANCE_ID), eq(VERSION_ID))).thenThrow(new NotFoundException());

        final Optional<Version> result = kioVersionProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getVersionId();
        verify(kioOperationsMock).getApplicationVersion(eq(INSTANCE_ID), eq(VERSION_ID));
    }

}
