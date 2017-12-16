package com.maxml.timer.api;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.maxml.timer.api.interfaces.OnDbResult;
import com.maxml.timer.api.interfaces.OnResultList;
import com.maxml.timer.entity.Point;
import com.maxml.timer.entity.Slice;
import com.maxml.timer.entity.Table;
import com.maxml.timer.util.Constants;
import com.maxml.timer.util.NetworkStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SliceCRUD implements OnDbResult {

    private DatabaseReference sliceRef;
    private Handler handler;

    public OnResultList onresultList;
    private int readCount = 0;
    SliceCRUD sliceCRUD = this;

    public SliceCRUD(Handler handler) {
        if (sliceRef == null) {
            sliceRef = FirebaseDatabase.getInstance().getReference().child(Constants.SLICE_DATABASE_PATH);
        }
        this.handler = handler;
    }

    public void create(Slice slice) {
        Log.i("Slice", " Slice starting create");
        // get Firebase id
        String key = sliceRef.push().getKey();
        // set id entity
        slice.setId(key);
        try {
            sliceRef.child(key).setValue(slice).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        handler.sendEmptyMessage(Constants.DB_RESULT_OK);
                    } else {
                        handler.sendEmptyMessage(Constants.DB_RESULT_FALSE);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//	@Override
//	// linieCRUD - read or create
//	public void onResult(final ParseObject parseLine, Slice slice) {
//
//		final ParseObject parseSlice = new ParseObject("Slice");
//		parseSlice.put("User", slice.getUser());
//		parseSlice.put("startDate", slice.getStartDate());
//		parseSlice.put("endDate", slice.getEndDate());
//		parseSlice.put("Description", slice.getDescription());
//		if (slice.getType().equals(SliceType.CALL))
//			parseSlice.put("SliceType", "CALL");
//		if (slice.getType().equals(SliceType.WALK))
//			parseSlice.put("SliceType", "WALK");
//		if (slice.getType().equals(SliceType.WORK))
//			parseSlice.put("SliceType", "WORK");
//		if (slice.getType().equals(SliceType.REST))
//			parseSlice.put("SliceType", "REST");
//		parseSlice.put("UUID", "" + UUID.randomUUID());
//		parseSlice.put("Line", parseLine);
//		parseSlice.put("LineUUID", "" + parseLine.getString("UUID"));
//		parseSlice.put("deleted", false);
//		if (NetworkStatus.isConnected)
//			parseSlice.saveInBackground(
//
//			new SaveCallback() {
//				@Override
//				public void done(ParseException e) {
//					parseSlice.pinInBackground();
//					Log.i("Slice", "Slice is created id: " + parseSlice.getObjectId() + " Line UUID: "
//							+ parseLine.getString("UUID"));
//				}
//			}
//
//			);
//		if (!NetworkStatus.isConnected) {
//			parseSlice.saveInBackground();
//			parseSlice.pinInBackground();
//			Log.i("Slice", "Slice is created offline, id:" + parseSlice.getString("UUID"));
//			// parseSlice.saveEventually();
//		}
//
//	}

    public void read(String user) {
        if (user == null) return;
        sliceRef.orderByChild(Constants.SLICE_USER)
                .equalTo(user)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            List<Slice> list = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Slice slice = snapshot.getValue(Slice.class);
                                if (!slice.isDeleted()) {
                                    list.add(slice);
                                }
                            }
                            // send result
                            Message m = handler.obtainMessage(Constants.DB_RESULT_LIST, list);
                            handler.sendMessage(m);
                        } else {
                            handler.sendEmptyMessage(Constants.DB_RESULT_FALSE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        handler.sendEmptyMessage(Constants.DB_RESULT_FALSE);
                    }
                });
//		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
//		if (!NetworkStatus.isConnected)
//			query.fromLocalDatastore();
//		query.whereEqualTo("User", user);
//		query.findInBackground(new FindCallback<ParseObject>() {
//			public void done(List<ParseObject> parseSliceList, ParseException e) {
//				if (parseSliceList == null) {
//					Log.i("Slice", "The getFirst request failed.");
//
//				} else {
//
//					if (parseSliceList.size() == 0) {
//						List<Slice> sliceListNull1 = new ArrayList<Slice>();
//						LineCRUD lineCRUD = new LineCRUD();
//						sliceListNull1.add(new Slice("", null, new Date(), new Date(), "your list is emputy",
//								SliceType.REST));
//						lineCRUD.onresultLine = sliceCRUD;
//						lineCRUD.emputySliceList(sliceListNull1);
//					} else {
//
//						Log.i("SliceRead", "Starting read");
//						List<Slice> sliceList = new ArrayList<Slice>();
//						for (ParseObject parseSlice : parseSliceList) {
//							Log.i("SliceRead", "Slice: " + parseSlice.getString("UUID"));
//
//							Slice slice = new Slice();
//							slice.setUser(parseSlice.getString("User"));
//							slice.setStartDate(parseSlice.getDate("startDate"));
//							slice.setEndDate(parseSlice.getDate("endDate"));
//							slice.setDescription(parseSlice.getString("Description"));
//							slice.setUpdateDate(parseSlice.getUpdatedAt());
//
//							String sliceType = parseSlice.getString("SliceType");
//							if (sliceType.equals("WALK"))
//								slice.setType(SliceType.WALK);
//							if (sliceType.equals("CALL"))
//								slice.setType(SliceType.CALL);
//							if (sliceType.equals("REST"))
//								slice.setType(SliceType.REST);
//							if (sliceType.equals("WORK"))
//								slice.setType(SliceType.WORK);
//							slice.setId(parseSlice.getString("UUID"));
//							slice.setLineUUID(parseSlice.getString("LineUUID"));
//
//							if (!parseSlice.getBoolean("deleted")) {
//								sliceList.add(slice);
//								LineCRUD lineCRUD = new LineCRUD();
//								lineCRUD.onresultLine = sliceCRUD;
//								lineCRUD.read(parseSlice.getString("LineUUID"), sliceList);
//							}
//
//							Log.i("SliceRead", "" + parseSliceList.size());
//							Log.d("SliceCRUD_Special", "slice: " + parseSlice.getString("Description"));
//
//						}
//
//					}
//				}
//			}
//		});
//
    }

    @Override
    public void onResultRead(List<Slice> sliceList) {
        // TODO Auto-generated method stub
        readCount++;
        Log.i("SliceRead", " Read finish");
        if (readCount == sliceList.size()) {
            Log.i("SliceRead", " Read finish 2");
            Log.i("Slice", "Slice list size: " + sliceList.size());
            Collections.sort(sliceList, Slice.sliceComparator);
            onresultList.OnResultSlices(sliceList);
        }
    }

    public void update(Slice slice) {
        // get Firebase id
        String key = slice.getId();
        // set id entity
        slice.setId(key);
        sliceRef.child(key).setValue(slice).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    handler.sendEmptyMessage(Constants.DB_RESULT_OK);
                } else {
                    handler.sendEmptyMessage(Constants.DB_RESULT_FALSE);
                }
            }
        });

//		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
//		if (!NetworkStatus.isConnected)
//			query.fromLocalDatastore();
//
//		query.whereEqualTo("UUID", "" + slice.getId());
//		query.getFirstInBackground(new GetCallback<ParseObject>() {
//			public void done(ParseObject parseSlice, ParseException e) {
//				if (parseSlice == null) {
//					Log.i("SliceUpdate", "Update: The getFirst request failed.");
//				} else {
//
//					try {
//
//						parseSlice.fetch();
//						Log.i("SliceUpdate", "Starting update");
//						parseSlice.put("User", slice.getUser());
//						parseSlice.put("startDate", slice.getStartDate());
//						parseSlice.put("endDate", slice.getEndDate());
//						parseSlice.put("Description", slice.getDescription());
//						if (slice.getType().equals(SliceType.CALL))
//							parseSlice.put("SliceType", "CALL");
//						if (slice.getType().equals(SliceType.WALK))
//							parseSlice.put("SliceType", "WALK");
//						if (slice.getType().equals(SliceType.WORK))
//							parseSlice.put("SliceType", "WORK");
//						if (slice.getType().equals(SliceType.REST))
//							parseSlice.put("SliceType", "REST");
//						parseSlice.put("UUID", "" + UUID.randomUUID());
//						parseSlice.put("LineUUID", "" + slice.getPath().getId());
//						LineCRUD lineCRUD = new LineCRUD();
//						lineCRUD.update(slice);
//						PointCRUD pointCRUD = new PointCRUD();
//						pointCRUD.update(slice.getPath().getStart());
//						pointCRUD.update(slice.getPath().getFinish());
//						parseSlice.saveInBackground();
//						parseSlice.pinInBackground();
//						Log.i("Slice", "Slice is update, UUID:" + parseSlice.getString("UUID"));
//						parseSlice.saveEventually();
//
//					} catch (ParseException e1) {
//						// TODO Auto-generated catch block
//						e1.printStackTrace();
//					}
//
//				}
//			}
//		});
    }

    public void delete(String id) {
        sliceRef.child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    handler.sendEmptyMessage(Constants.RESULT_OK);
                } else {
                    handler.sendEmptyMessage(Constants.RESULT_FALSE);
                }
            }
        });
//		ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
//		if (!NetworkStatus.isConnected)
//			query.fromLocalDatastore();
//
//		query.whereEqualTo("UUID", "" + id);
//		query.getFirstInBackground(new GetCallback<ParseObject>() {
//			public void done(ParseObject parseSlice, ParseException e) {
//				if (parseSlice == null) {
//
//					Log.i("Slice", "Deleted: The getFirst request failed.");
//				} else {
//					Log.i("Slice", "Slice " + id + " is deleted");
//					parseSlice.put("deleted", true);
//					parseSlice.pinInBackground();
//					parseSlice.saveInBackground();
//				}
//			}
//		});
    }

    public void sync(Table table) {
        Log.i("Slice", "" + NetworkStatus.isConnected);

        Map<String,Object> updateList = new HashMap<>();
        Log.i("Slice", "Slice synchronized start");
        for (Slice slice : table.getList()) {
            // if slice already exist in DB it has no null id field
            // if field null - save new slice to DB
            if (slice.getId() == null) {
                create(slice);
            } else {
                updateList.put(slice.getId(), slice);
            }
        }
        // update items from updateList in DB
        sliceRef.updateChildren(updateList).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    handler.sendEmptyMessage(Constants.RESULT_OK);
                } else {
                    handler.sendEmptyMessage(Constants.RESULT_FALSE);
                }
            }
        });

//				ParseQuery<ParseObject> query = ParseQuery.getQuery("Slice");
//				if (!NetworkStatus.isConnected)
//					query.fromLocalDatastore();
//
//				query.whereEqualTo("UUID", slice.getId());
//				query.getFirstInBackground(new GetCallback<ParseObject>() {
//					public void done(ParseObject parseSlice, ParseException e) {
//						if (parseSlice == null) {
//							Log.i("Slice", "Sync: The getFirst request failed.");
//						} else {
//							if (!slice.getUpdateDate().equals(parseSlice.getUpdatedAt()))// ----------
//								try {
//									update(slice);
//								} catch (InterruptedException e1) {
//									// TODO Auto-generated catch block
//									e1.printStackTrace();
//								}
//							Log.i("Slice", "Sync: update dont need");
//						}
//					}
//				});
//			}
//		}

    }

    @Override
    public void onResult(Point point, List<Slice> sliceList) {
        // TODO Auto-generated method stub

    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

//	@Override
//	public void onResult(ParseObject parsePoint, ParseObject parsePointFinish, Slice slice) {
//		// TODO Auto-generated method stub
//
//	}

}
