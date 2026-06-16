package com.limelight.nvstream;

import com.limelight.nvstream.http.ComputerDetails;

import java.security.cert.X509Certificate;

import javax.crypto.SecretKey;

public class ConnectionContext {
    public ComputerDetails.AddressTuple serverAddress;
    public int httpsPort;
    // GenStream patch: nếu != 0, moonlight-common-c suy ra port stream từ base port này
    // (relay offset-forwarding). 0 = hành vi Moonlight gốc.
    public int serverPortBase;
    public boolean isNvidiaServerSoftware;
    public X509Certificate serverCert;
    public StreamConfiguration streamConfig;
    public NvConnectionListener connListener;
    public SecretKey riKey;
    public int riKeyId;
    
    // This is the version quad from the appversion tag of /serverinfo
    public String serverAppVersion;
    public String serverGfeVersion;
    public int serverCodecModeSupport;

    // This is the sessionUrl0 tag from /resume and /launch
    public String rtspSessionUrl;
    
    public int negotiatedWidth, negotiatedHeight;
    public boolean negotiatedHdr;

    public int negotiatedRemoteStreaming;
    public int negotiatedPacketSize;

    public int videoCapabilities;
}
