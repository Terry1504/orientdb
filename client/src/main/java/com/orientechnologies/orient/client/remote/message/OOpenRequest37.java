package com.orientechnologies.orient.client.remote.message;

import com.orientechnologies.orient.client.binary.OBinaryRequestExecutor;
import com.orientechnologies.orient.client.remote.OBinaryRequest;
import com.orientechnologies.orient.client.remote.OBinaryResponse;
import com.orientechnologies.orient.client.remote.OStorageRemoteSession;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinaryProtocol;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataInput;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataOutput;

import java.io.IOException;

public class OOpenRequest37 implements OBinaryRequest<OOpenResponse> {

  private String  clientId     = null;
  private boolean useToken     = true;
  private boolean supportsPush = true;
  private boolean collectStats = true;
  private String databaseName;
  private String userName;
  private String userPassword;

  public OOpenRequest37(String databaseName, String userName, String userPassword) {
    this.databaseName = databaseName;
    this.userName = userName;
    this.userPassword = userPassword;
  }

  public OOpenRequest37() {

  }

  @Override
  public void write(OChannelDataOutput network, OStorageRemoteSession session) throws IOException {
    network.writeString(clientId);
    network.writeBoolean(useToken);
    network.writeBoolean(supportsPush);
    network.writeBoolean(collectStats);
    network.writeString(databaseName);
    network.writeString(userName);
    network.writeString(userPassword);
  }

  @Override
  public void read(OChannelDataInput channel, int protocolVersion, ORecordSerializer serializer) throws IOException {

    clientId = channel.readString();

    useToken = channel.readBoolean();
    supportsPush = channel.readBoolean();
    collectStats = channel.readBoolean();

    databaseName = channel.readString();
    userName = channel.readString();
    userPassword = channel.readString();
  }

  @Override
  public byte getCommand() {
    return OChannelBinaryProtocol.REQUEST_DB_OPEN;
  }

  @Override
  public String getDescription() {
    return "Open Database";
  }

  public String getDatabaseName() {
    return databaseName;
  }

  public String getUserName() {
    return userName;
  }

  public String getUserPassword() {
    return userPassword;
  }

  public String getClientId() {
    return clientId;
  }


  public boolean isCollectStats() {
    return collectStats;
  }

  public boolean isSupportsPush() {
    return supportsPush;
  }

  public boolean isUseToken() {
    return useToken;
  }

  @Override
  public boolean requireDatabaseSession() {
    return false;
  }

  @Override
  public OOpenResponse createResponse() {
    return new OOpenResponse();
  }

  @Override
  public OBinaryResponse execute(OBinaryRequestExecutor executor) {
    return executor.executeDatabaseOpen37(this);
  }

}