package phy.jsf.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {
    public String task_id;
    public String form_id;
    public int form_type;
    public String form_name;
    public int form_day_night;
    public String device_id;
    public String device_name;
    public String building;
    public String device_floor;
    public String device_room;
    public String content;
    public String edit_content;
    public long scheduler_time;
    public long create_time;
    public int state;
    public String user_id;
    public String check_user;
    public long check_time;
    public long upload_time;
    public long commit_time;
    public int upload_state;
    public long scan_time;


    public Task() {
    }

    protected Task(Parcel in) {
        task_id = in.readString();
        form_id = in.readString();
        form_type = in.readInt();
        form_name = in.readString();
        form_day_night = in.readInt();
        device_id = in.readString();
        device_name = in.readString();
        building = in.readString();
        device_floor = in.readString();
        device_room = in.readString();
        content = in.readString();
        edit_content=in.readString();
        scheduler_time = in.readLong();
        create_time = in.readLong();
        state = in.readInt();
        user_id = in.readString();
        check_user = in.readString();
        check_time = in.readLong();
        upload_time = in.readLong();
        commit_time = in.readLong();
        upload_state=in.readInt();
        scan_time=in.readLong();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(task_id);
        dest.writeString(form_id);
        dest.writeInt(form_type);
        dest.writeString(form_name);
        dest.writeInt(form_day_night);
        dest.writeString(device_id);
        dest.writeString(device_name);
        dest.writeString(building);
        dest.writeString(device_floor);
        dest.writeString(device_room);
        dest.writeString(content);
        dest.writeString(edit_content);
        dest.writeLong(scheduler_time);
        dest.writeLong(create_time);
        dest.writeInt(state);
        dest.writeString(user_id);
        dest.writeString(check_user);
        dest.writeLong(check_time);
        dest.writeLong(upload_time);
        dest.writeLong(commit_time);
        dest.writeInt(upload_state);
        dest.writeLong(scan_time);
    }


    public void setCommit_time(long commit_time) {
        this.commit_time = commit_time;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public void setForm_id(String form_id) {
        this.form_id = form_id;
    }

    public void setForm_type(int form_type) {
        this.form_type = form_type;
    }

    public void setForm_name(String form_name) {
        this.form_name = form_name;
    }

    public void setForm_day_night(int form_day_night) {
        this.form_day_night = form_day_night;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public void setDevice_floor(String device_floor) {
        this.device_floor = device_floor;
    }

    public void setDevice_room(String device_room) {
        this.device_room = device_room;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setScheduler_time(long scheduler_time) {
        this.scheduler_time = scheduler_time;
    }

    public void setUpload_time(long upload_time) {
        this.upload_time = upload_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setCheck_user(String check_user) {
        this.check_user = check_user;
    }

    public void setCheck_time(long check_time) {
        this.check_time = check_time;
    }

    public void setEdit_content(String edit_content) {
        this.edit_content = edit_content;
    }

    public void setScan_time(long scan_time) {
        this.scan_time = scan_time;
    }
}
