package com.maxml.timer.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.maxml.timer.controllers.Controller;
import com.maxml.timer.entity.DbReturnData;
import com.maxml.timer.entity.User;
import com.maxml.timer.entity.actions.Action;
import com.maxml.timer.entity.eventBus.DbMessage;
import com.maxml.timer.entity.eventBus.EventMessage;
import com.maxml.timer.util.Constants;
import com.maxml.timer.util.EventType;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class ActionCRUD {

    private EventBus eventBus;
    private DatabaseReference actionRef;

    public ActionCRUD(Context context) {
        Controller controller = new Controller(context);
        eventBus = controller.getEventBus(EventType.DB_EVENT_BUS);
        User user = UserAPI.getCurrentUser();
        if (actionRef == null && user != null) {
            actionRef = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.USER_DATABASE_PATH)
                    .child(user.getId());
        }
    }

    public void create(Action action, final DbReturnData returnData) {
        Log.i("Slice", " Slice starting create");
        // get Firebase id
        String dbId = actionRef.push().getKey();
        action.setId(dbId);

        actionRef.child(dbId).setValue(action).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    eventBus.post(new DbMessage(Constants.EVENT_DB_RESULT_OK, returnData));
                } else {
                    eventBus.post(new DbMessage(Constants.EVENT_DB_RESULT_ERROR, returnData));
                }
            }
        });
    }

/*
    private void saveIdAndDay(long dayCount, String id) {
        // create update map
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> data = new HashMap<>();
        data.put("/" + Constants.CALL_DATABASE_PATH + "/" + id, dayCount);
        rootRef.updateChildren(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    eventBus.post(new DbMessage(Constants.EVENT_DB_RESULT_OK));
                } else {
                    eventBus.post(new DbMessage(Constants.EVENT_DB_RESULT_ERROR));
                }
            }
        });
    }
*/


//    public void read(String id, final DbMessage messageForResult) {
//        if (id == null) {
//            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//            return;
//        }
//        actionRef.child(Constants.CALL_DATABASE_PATH)
//                .orderByKey()
//                .equalTo(id)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            String dayCount = dataSnapshot.getValue(String.class);
//                            read(Utils.getDate(dayCount), messageForResult);
//                            return;
//                        }
//                        EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_OK, messageForResult));
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
//    public void read(Date date, final DbMessage messageForResult) {
//        if (date == null) {
//            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//            return;
//        }
//        actionRef.child(Utils.getDayCount(date) + "")
//                .child(Constants.CALL_DATABASE_PATH)
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        if (dataSnapshot.exists()) {
//                            List<CallAction> list = new ArrayList<>();
//                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                                CallAction call = snapshot.getValue(CallAction.class);
//                                if (!call.isDeleted()) {
//                                    list.add(call);
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


//    public void update(String id, Map<String, Object> changes) {
//        if (id == null || id.equals("") || changes == null) {
//            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//            return;
//        }
//
//        actionRef.child(Constants.CALL_DATABASE_PATH)
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
//    public void delete(final CallAction call) {
//        if (call == null) {
//            EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//            return;
//        }
//        final String id = call.getId();
//        actionRef.child(Constants.CALL_DATABASE_PATH)
//                .child(id)
//                .removeValue()
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            actionRef.child(Constants.SORT_BY_DATE_PATH)
//                                    .child(Utils.getDayCount(call.getStartDate()) + "")
//                                    .child(Constants.CALL_DATABASE_PATH)
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
//        Map<String, Object> updateList = new HashMap<>();
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
//        actionRef.updateChildren(updateList).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task) {
//                if (task.isSuccessful()) {
//                    EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_OK));
//                } else {
//                    EventBus.getDefault().post(new DbResultMessage(Constants.EVENT_DB_RESULT_ERROR));
//                }
//            }
//        });
//
//
//    }
}
