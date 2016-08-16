package com.softdesign.devintensive.data.storage.models;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "LIKES_BY".
*/
public class LikesByDao extends AbstractDao<LikesBy, String> {

    public static final String TABLENAME = "LIKES_BY";

    /**
     * Properties of entity LikesBy.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property SenderRemoteId = new Property(0, String.class, "senderRemoteId", false, "SENDER_REMOTE_ID");
        public final static Property RecipientRemoteId = new Property(1, String.class, "recipientRemoteId", false, "RECIPIENT_REMOTE_ID");
        public final static Property Sender_recipient = new Property(2, String.class, "sender_recipient", true, "SENDER_RECIPIENT");
    };

    private DaoSession daoSession;

    private Query<LikesBy> user_LikesByListQuery;

    public LikesByDao(DaoConfig config) {
        super(config);
    }
    
    public LikesByDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"LIKES_BY\" (" + //
                "\"SENDER_REMOTE_ID\" TEXT NOT NULL ," + // 0: senderRemoteId
                "\"RECIPIENT_REMOTE_ID\" TEXT NOT NULL ," + // 1: recipientRemoteId
                "\"SENDER_RECIPIENT\" TEXT PRIMARY KEY NOT NULL UNIQUE );"); // 2: sender_recipient
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"LIKES_BY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, LikesBy entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getSenderRemoteId());
        stmt.bindString(2, entity.getRecipientRemoteId());
        stmt.bindString(3, entity.getSender_recipient());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, LikesBy entity) {
        stmt.clearBindings();
        stmt.bindString(1, entity.getSenderRemoteId());
        stmt.bindString(2, entity.getRecipientRemoteId());
        stmt.bindString(3, entity.getSender_recipient());
    }

    @Override
    protected final void attachEntity(LikesBy entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public String readKey(Cursor cursor, int offset) {
        return cursor.getString(offset + 2);
    }    

    @Override
    public LikesBy readEntity(Cursor cursor, int offset) {
        LikesBy entity = new LikesBy( //
            cursor.getString(offset + 0), // senderRemoteId
            cursor.getString(offset + 1), // recipientRemoteId
            cursor.getString(offset + 2) // sender_recipient
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, LikesBy entity, int offset) {
        entity.setSenderRemoteId(cursor.getString(offset + 0));
        entity.setRecipientRemoteId(cursor.getString(offset + 1));
        entity.setSender_recipient(cursor.getString(offset + 2));
     }
    
    @Override
    protected final String updateKeyAfterInsert(LikesBy entity, long rowId) {
        return entity.getSender_recipient();
    }
    
    @Override
    public String getKey(LikesBy entity) {
        if(entity != null) {
            return entity.getSender_recipient();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "likesByList" to-many relationship of User. */
    public List<LikesBy> _queryUser_LikesByList(String recipientRemoteId) {
        synchronized (this) {
            if (user_LikesByListQuery == null) {
                QueryBuilder<LikesBy> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.RecipientRemoteId.eq(null));
                user_LikesByListQuery = queryBuilder.build();
            }
        }
        Query<LikesBy> query = user_LikesByListQuery.forCurrentThread();
        query.setParameter(0, recipientRemoteId);
        return query.list();
    }

}
