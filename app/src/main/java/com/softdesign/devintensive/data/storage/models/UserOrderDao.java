package com.softdesign.devintensive.data.storage.models;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "USER_ORDER".
*/
public class UserOrderDao extends AbstractDao<UserOrder, Long> {

    public static final String TABLENAME = "USER_ORDER";

    /**
     * Properties of entity UserOrder.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property UserRemoteId = new Property(1, String.class, "userRemoteId", false, "USER_REMOTE_ID");
        public final static Property UserOrder = new Property(2, int.class, "userOrder", false, "USER_ORDER");
    };

    private DaoSession daoSession;


    public UserOrderDao(DaoConfig config) {
        super(config);
    }
    
    public UserOrderDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"USER_ORDER\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"USER_REMOTE_ID\" TEXT," + // 1: userRemoteId
                "\"USER_ORDER\" INTEGER NOT NULL );"); // 2: userOrder
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"USER_ORDER\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, UserOrder entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String userRemoteId = entity.getUserRemoteId();
        if (userRemoteId != null) {
            stmt.bindString(2, userRemoteId);
        }
        stmt.bindLong(3, entity.getUserOrder());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, UserOrder entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String userRemoteId = entity.getUserRemoteId();
        if (userRemoteId != null) {
            stmt.bindString(2, userRemoteId);
        }
        stmt.bindLong(3, entity.getUserOrder());
    }

    @Override
    protected final void attachEntity(UserOrder entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public UserOrder readEntity(Cursor cursor, int offset) {
        UserOrder entity = new UserOrder( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // userRemoteId
            cursor.getInt(offset + 2) // userOrder
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, UserOrder entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUserRemoteId(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUserOrder(cursor.getInt(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(UserOrder entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(UserOrder entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
