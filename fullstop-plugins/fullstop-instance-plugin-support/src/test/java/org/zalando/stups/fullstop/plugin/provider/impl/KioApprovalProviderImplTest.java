package org.zalando.stups.fullstop.plugin.provider.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.clients.kio.Approval;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.provider.KioApprovalProvider;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class KioApprovalProviderImplTest {

    private static final String INSTANCE_ID = "i-1234";
    private static final String VERSION_ID = "1.0";
    private KioApprovalProvider kioApprovalProvider;
    private EC2InstanceContext ec2InstanceContextMock;
    private KioOperations kioOperationsMock;


    @Before
    public void setUp() {

        kioOperationsMock = mock(KioOperations.class);
        kioApprovalProvider = new KioApprovalProviderImpl(kioOperationsMock);

        ec2InstanceContextMock = mock(EC2InstanceContext.class);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(ec2InstanceContextMock, kioOperationsMock);
    }

    @Test
    public void testApprovalFound() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));

        Version version = new Version();
        version.setId("1.0");

        when(ec2InstanceContextMock.getKioVersion()).thenReturn(Optional.of(version));

        when(kioOperationsMock.getApplicationVersionApprovals(eq(INSTANCE_ID), eq(VERSION_ID))).thenReturn(newArrayList(new Approval()));

        List<Approval> result = kioApprovalProvider.apply(ec2InstanceContextMock);
        assertThat(result).isNotEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getKioVersion();
        verify(kioOperationsMock).getApplicationVersionApprovals(eq(INSTANCE_ID), eq(VERSION_ID));
    }

    @Test
    public void testApprovalNotFound1() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.of(INSTANCE_ID));


        when(ec2InstanceContextMock.getKioVersion()).thenReturn(Optional.empty());


        List<Approval> result = kioApprovalProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getKioVersion();
    }

    @Test
    public void testApprovalNotFound2() throws Exception {
        when(ec2InstanceContextMock.getApplicationId()).thenReturn(Optional.empty());

        Version version = new Version();
        version.setId("1.0");

        when(ec2InstanceContextMock.getKioVersion()).thenReturn(Optional.of(version));


        List<Approval> result = kioApprovalProvider.apply(ec2InstanceContextMock);
        assertThat(result).isEmpty();

        verify(ec2InstanceContextMock).getApplicationId();
        verify(ec2InstanceContextMock).getKioVersion();
    }


}