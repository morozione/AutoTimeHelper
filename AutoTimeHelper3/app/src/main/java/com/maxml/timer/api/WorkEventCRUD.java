package com.maxml.timer.api;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maxml.timer.entity.User;
import com.maxml.timer.entity.actions.WorkAction;
import com.maxml.timer.entity.eventBus.dbMessage.DbResultMessage;
import com.maxml.timer.util.Constants;
import com.maxml.timer.util.Utils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkEventCRUD {

    private DatabaseReference userRef;

    public WorkEventCRUD() {
        User user = UserAPI.getCurrentUser();
        if (userRef == null && user != null) {
            userRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.USER_DATABASE_PATH)
                    .child(user.getId());
        }
    }

    public void create(WorkAction workAction) {
        // get Firebase id
        final String dbId = userRef.push().getKey();
        workAction.setId(dbId);

        // put data for sort by day
        final long dayCount = Utils.getDayCount(workAction.getStartDate());
        if (dayCount == -1) {
            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
            return;
        }

        userRef.child(dayCount+"")
        .child(Constants.WORK_DATABASE_PATH)
        .child(dbId)
        .setValue(workAction).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    saveIdAndDay(dayCount, dbId);
                } else {
                    EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
                }
            }
        });
    }

    private void saveIdAndDay(long dayCount, String id) {
        // create update map
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> data = new HashMap<>();
        data.put("/" + Constants.WORK_DATABASE_PATH + "/" + id, dayCount);
        rootRef.updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_OK));
                } else {
                    EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
                }
            }
        });
    }


/*
    public void read(String id) {
        if (id == null) {
            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
            return;
        }
        userRef.child(Constants.WORK_DATABASE_PATH)
                .orderByChild(Constants.COLUMN_ID)
                .equalTo(id)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<WorkAction> list = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                WorkAction work = snapshot.getValue(WorkAction.class);
                                if (!work.isDeleted()) {
                                    list.add(work);
                                }
                            }
                            // send result
                            if (list.size() > 0) {
                                EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_LIST, list.get(0)));
                            } else {
                                EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
                            }

                        } else {
                            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
                    }
                });
    }
*/

//    public void read(Date date) {
//        if (date == null) {
//            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//            return;
//        }
//        userRef.child(Utils.getDayCount(date) + "")
//                .child(Constants.WORK_DATABASE_PATH)
//                .orderByValue().addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            List<WorkAction> list = new ArrayList<>();
//                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                WorkAction work = snapshot.getValue(WorkAction.class);
//                                if (!work.isDeleted()) {
//                                    list.add(work);
//                                }
//                            }
//                            // send result
//                            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_LIST, list));
//
//                        } else {
//                            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//                        EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//                    }
//                });
//    }
//
//
//    public void update(String id, Map<String, Object> changes) {
//        if (id == null || id.equals("") || changes == null) {
//            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//            return;
//        }
//
//        userRef.child(Constants.WORK_DATABASE_PATH)
//                .child(id)
//                .updateChildren(changes).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_OK));
//                } else {
//                    EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//                }
//            }
//        });
//    }
//
//    public void delete(final WorkAction work) {
//        if (work == null) {
//            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//            return;
//        }
//        final String id = work.getId();
//        userRef.child(Constants.WORK_DATABASE_PATH)
//                .child(id)
//                .removeValue()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            userRef.child(Constants.SORT_BY_DATE_PATH)
//                                    .child(Utils.getDayCount(work.getStartDate()) + "")
//                                    .child(Constants.WORK_DATABASE_PATH)
//                                    .child(id)
//                                    .removeValue()
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_OK));
//                                            } else {
//                                                EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//                                            }
//                                        }
//                                    });
//
//                        } else {
//                            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//                        }
//                    }
//                });
//    }
//    public void sync(Table table) {
//        Log.i("Slice", "" + NetworkStatus.isConnected);
//
//        Map<String,Object> updateList = new HashMap<>();
//        Log.i("Slice", "Slice synchronized start");
//        for (Slice slice : table.getList()) {
//            // if slice already exist in DB it has no null id field
//            // if field null - save new slice to DB
//            if (slice.getId() == null) {
//                create(slice);
//            } else {
//                updateList.put(slice.getId(), slice);
//            }
//        }
//        // update items from updateList in DB
//        sliceRef.updateChildren(updateList).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    handler.sendEmptyMessage(Constants.RESULT_OK);
//                } else {
//                    handler.sendEmptyMessage(Constants.RESULT_FALSE);
//                }
//            }
//        });
//    }
}
