package com.orientechnologies.orient.server.distributed.impl.coordinator.transaction;

import com.orientechnologies.orient.client.remote.message.OCommit37Response;
import com.orientechnologies.orient.core.id.ORID;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.server.distributed.impl.coordinator.OSubmitResponse;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.orientechnologies.orient.server.distributed.impl.coordinator.OCoordinateMessagesFactory.TRANSACTION_SUBMIT_RESPONSE;

public class OTransactionResponse implements OSubmitResponse {
  private boolean success;

  private List<OCreatedRecordResponse> createdRecords;
  private List<OUpdatedRecordResponse> updatedRecords;
  private List<ODeletedRecordResponse> deletedRecords;

  public OTransactionResponse(boolean success, List<OCreatedRecordResponse> createdRecords,
      List<OUpdatedRecordResponse> updatedRecords, List<ODeletedRecordResponse> deletedRecords) {
    this.success = success;
    this.createdRecords = createdRecords;
    this.updatedRecords = updatedRecords;
    this.deletedRecords = deletedRecords;
  }

  public OTransactionResponse() {

  }

  @Override
  public void serialize(DataOutput output) throws IOException {
    output.writeBoolean(success);
    output.writeInt(createdRecords.size());
    for (OCreatedRecordResponse createdRecord : createdRecords) {
      createdRecord.serialize(output);
    }
    output.writeInt(updatedRecords.size());
    for (OUpdatedRecordResponse updatedRecord : updatedRecords) {
      updatedRecord.serialize(output);
    }
    output.writeInt(deletedRecords.size());
    for (ODeletedRecordResponse deletedRecord : deletedRecords) {
      deletedRecord.serialize(output);
    }
  }

  @Override
  public void deserialize(DataInput input) throws IOException {
    success = input.readBoolean();
    int createSize = input.readInt();
    createdRecords = new ArrayList<>(createSize);
    while (createSize-- > 0) {
      OCreatedRecordResponse createResponse = new OCreatedRecordResponse();
      createResponse.deserialize(input);
      createdRecords.add(createResponse);
    }
    int updateSize = input.readInt();
    updatedRecords = new ArrayList<>(updateSize);
    while (updateSize-- > 0) {
      OUpdatedRecordResponse updateRecord = new OUpdatedRecordResponse();
      updateRecord.deserialize(input);
      updatedRecords.add(updateRecord);
    }
    int deleteSize = input.readInt();
    deletedRecords = new ArrayList<>(deleteSize);
    while (deleteSize-- > 0) {
      ODeletedRecordResponse deletedResponse = new ODeletedRecordResponse();
      deletedResponse.deserialize(input);
      deletedRecords.add(deletedResponse);
    }
  }

  @Override
  public int getResponseType() {
    return TRANSACTION_SUBMIT_RESPONSE;
  }

  public boolean isSuccess() {
    return success;
  }

  public List<OUpdatedRecordResponse> getUpdatedRecords() {
    return updatedRecords;
  }

  public List<ODeletedRecordResponse> getDeletedRecords() {
    return deletedRecords;
  }

  public List<OCreatedRecordResponse> getCreatedRecords() {
    return createdRecords;
  }
}
