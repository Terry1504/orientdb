package com.orientechnologies.orient.client.remote.message;

import com.orientechnologies.orient.client.binary.OBinaryRequestExecutor;
import com.orientechnologies.orient.client.remote.OBinaryRequest;
import com.orientechnologies.orient.client.remote.OBinaryResponse;
import com.orientechnologies.orient.client.remote.OStorageRemoteSession;
import com.orientechnologies.orient.client.remote.message.tx.IndexChange;
import com.orientechnologies.orient.client.remote.message.tx.ORecordOperationRequest;
import com.orientechnologies.orient.core.db.record.ORecordOperation;
import com.orientechnologies.orient.core.record.ORecordInternal;
import com.orientechnologies.orient.core.serialization.serializer.record.ORecordSerializer;
import com.orientechnologies.orient.core.serialization.serializer.record.binary.ORecordSerializerNetworkV37;
import com.orientechnologies.orient.core.tx.OTransactionIndexChanges;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelBinaryProtocol;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataInput;
import com.orientechnologies.orient.enterprise.channel.binary.OChannelDataOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OBeginTransactionRequest implements OBinaryRequest<OBinaryResponse> {

  private int                           txId;
  private boolean                       usingLog;
  private List<ORecordOperationRequest> operations;
  private List<IndexChange>             indexChanges;

  public OBeginTransactionRequest(int txId, boolean usingLog, Iterable<ORecordOperation> operations,
      Map<String, OTransactionIndexChanges> indexChanges) {
    super();
    this.txId = txId;
    this.usingLog = usingLog;
    this.indexChanges = new ArrayList<>();

    List<ORecordOperationRequest> netOperations = new ArrayList<>();
    for (ORecordOperation txEntry : operations) {
      if (txEntry.type == ORecordOperation.LOADED)
        continue;
      ORecordOperationRequest request = new ORecordOperationRequest();
      request.setType(txEntry.type);
      request.setVersion(txEntry.getRecord().getVersion());
      request.setId(txEntry.getRecord().getIdentity());
      request.setRecordType(ORecordInternal.getRecordType(txEntry.getRecord()));
      switch (txEntry.type) {
      case ORecordOperation.CREATED:
      case ORecordOperation.UPDATED:
        request.setRecord(txEntry.getRecord());
        request.setContentChanged(ORecordInternal.isContentChanged(txEntry.getRecord()));
        break;
      }
      netOperations.add(request);
    }
    this.operations = netOperations;

    for (Map.Entry<String, OTransactionIndexChanges> change : indexChanges.entrySet()) {
      this.indexChanges.add(new IndexChange(change.getKey(), change.getValue()));
    }

  }

  public OBeginTransactionRequest() {
  }

  @Override
  public void write(OChannelDataOutput network, OStorageRemoteSession session) throws IOException {
    //from 3.0 the the serializer is bound to the protocol
    ORecordSerializerNetworkV37 serializer = ORecordSerializerNetworkV37.INSTANCE;

    network.writeInt(txId);
    network.writeBoolean(usingLog);

    for (ORecordOperationRequest txEntry : operations) {
      OMessageHelper.writeTransactionEntry(network, txEntry, serializer);
    }

    // END OF RECORD ENTRIES
    network.writeByte((byte) 0);

    // SEND MANUAL INDEX CHANGES
    OMessageHelper.writeTransactionIndexChanges(network, serializer, indexChanges);
  }

  @Override
  public void read(OChannelDataInput channel, int protocolVersion, ORecordSerializer serializer) throws IOException {
    txId = channel.readInt();
    usingLog = channel.readBoolean();
    operations = new ArrayList<>();
    byte hasEntry;
    do {
      hasEntry = channel.readByte();
      if (hasEntry == 1) {
        ORecordOperationRequest entry = OMessageHelper.readTransactionEntry(channel, serializer);
        operations.add(entry);
      }
    } while (hasEntry == 1);

    // RECEIVE MANUAL INDEX CHANGES
    this.indexChanges = OMessageHelper.readTransactionIndexChanges(channel, (ORecordSerializerNetworkV37) serializer);
  }

  @Override
  public byte getCommand() {
    return OChannelBinaryProtocol.REQUEST_TX_BEGIN;
  }

  @Override
  public OBinaryResponse createResponse() {
    return new OBeginTransactionResponse();
  }

  @Override
  public OBinaryResponse execute(OBinaryRequestExecutor executor) {
    return executor.executeBeginTransaction(this);
  }

  @Override
  public String getDescription() {
    return "Begin Transaction";
  }

  public List<ORecordOperationRequest> getOperations() {
    return operations;
  }

  public List<IndexChange> getIndexChanges() {
    return indexChanges;
  }

  public int getTxId() {
    return txId;
  }

  public boolean isUsingLog() {
    return usingLog;
  }
}
