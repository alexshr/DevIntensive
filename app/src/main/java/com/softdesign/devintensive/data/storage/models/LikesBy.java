package com.softdesign.devintensive.data.storage.models;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

@Entity(active = true, nameInDb = "LIKES_BY")
public class LikesBy {

 /*
    @Id
    @Unique
    private Long id;
*/
    /**
     * sender
     */
    @NotNull
    private String senderRemoteId;

    /**
     * recipient
     */
    @NotNull
    private String recipientRemoteId;

    //пока не рзобрался с уник индексом из 2 полей
    @Id
    @NotNull
    @Unique
    private String sender_recipient;

    /** Used for active entity operations. */
    @Generated(hash = 1721354425)
    private transient LikesByDao myDao;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    public LikesBy(String senderId, String recipientId) {
        senderRemoteId = senderId;
        recipientRemoteId = recipientId;
        sender_recipient=senderId+"_"+recipientId;

    }


    @Generated(hash = 1013386674)
    public LikesBy(@NotNull String senderRemoteId, @NotNull String recipientRemoteId,
            @NotNull String sender_recipient) {
        this.senderRemoteId = senderRemoteId;
        this.recipientRemoteId = recipientRemoteId;
        this.sender_recipient = sender_recipient;
    }


    @Generated(hash = 991161490)
    public LikesBy() {
    }

/*
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
*/
    public String getSenderRemoteId() {
        return senderRemoteId;
    }

    public String getRecipientRemoteId() {
        return recipientRemoteId;
    }

    @Override
    public String toString() {
        return "LikesBy{" +
                "senderRemoteId='" + senderRemoteId + '\'' +
                ", recipientRemoteId='" + recipientRemoteId + '\'' +
                ", sender_recipient='" + sender_recipient + '\'' +
                '}';
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }


    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }


    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 122203027)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLikesByDao() : null;
    }


    public void setRecipientRemoteId(String recipientRemoteId) {
        this.recipientRemoteId = recipientRemoteId;
    }


    public void setSenderRemoteId(String senderRemoteId) {
        this.senderRemoteId = senderRemoteId;
    }

    public String getSender_recipient() {
        return sender_recipient;
    }

    public void setSender_recipient(String sender_recipient) {
        this.sender_recipient = sender_recipient;
    }
}
