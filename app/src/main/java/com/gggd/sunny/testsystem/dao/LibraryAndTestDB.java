package com.gggd.sunny.testsystem.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.gggd.sunny.testsystem.bean.Library;
import com.gggd.sunny.testsystem.tools.DBOpenHelper;

import java.util.ArrayList;

/**
 * Created by Sunny on 2017/10/13.
 */

public class LibraryAndTestDB {

    private SQLiteDatabase db;

    public LibraryAndTestDB(Context context) {
        DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
        db = dbOpenHelper.getReadableDatabase();
    }

    public String insertLibraryTestCount(Library library, int max) {
        String minnumsql = "select num from library where id=(select max(id) from library)";
        Cursor cursor = db.rawQuery(minnumsql, null);
        int minnum = 1;
        int minid = 0;
        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            minnum = cursor.getInt(cursor.getColumnIndex("num"))+1;
        }
        String insersql = "insert into library(name,num,flag,single_num,multiple_num,judge_num) values(?,?,?,?,?,?)";
        for (int i = 0; i < max; i++) {
            db.execSQL(insersql, new Object[]{library.getLibrary_name(), minnum, 0, library.getSingle_num(),
                    library.getMultiple_num(), library.getJudge_num()});
        }
        String mintestid = "select min(id) from library where num=?";
        cursor = db.rawQuery(mintestid, new String[]{minnum+""});
        cursor.moveToFirst();
        minid = cursor.getInt(cursor.getColumnIndex("min(id)"))-1;
        if(cursor != null)
            cursor.close();
        return minnum + " " + minid;
    }
    //查看题库名是否重复
    public boolean libraryNameLimit(String libraryname) {
        boolean flag = false;
        String sql = "select name from library where name=?";
        Cursor cursor = db.rawQuery(sql, new String[]{libraryname});
        if (cursor.getCount() == 0) {
            flag = true;
        }
        if(cursor != null)
            cursor.close();
        return flag;
    }
    //开始考试，查询出一条未进行开始的记录
    public ArrayList<Library> getTestList(String sql1, String[] args){
        ArrayList<Library> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql1, args);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String num = cursor.getString(cursor.getColumnIndex("num"));
            String flag = cursor.getString(cursor.getColumnIndex("flag"));
            String single_num = cursor.getString(cursor.getColumnIndex("single_num"));
            String multiple_num = cursor.getString(cursor.getColumnIndex("multiple_num"));
            String judge_num = cursor.getString(cursor.getColumnIndex("judge_num"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            if(time == null )
                time = "";
            Library library = new Library(id,name,num,flag,single_num,multiple_num,judge_num,time);
            list.add(library);
        }
        if(cursor != null)
            cursor.close();
        return list;
    }

    //开始考试，查询出一条未进行开始的记录
    public ArrayList<Library> getTestsList(String sql1, String[] args){
        ArrayList<Library> list = new ArrayList<>();
        Cursor cursor = db.rawQuery(sql1, args);
        while (cursor.moveToNext()){
            int id = cursor.getInt(cursor.getColumnIndex("id"));
            String score = cursor.getString(cursor.getColumnIndex("score"));
            String time = cursor.getString(cursor.getColumnIndex("time"));
            Library library = new Library();
            library.setId(id);
            library.setScore(score);
            library.setTime(time);
            list.add(library);
        }
        if(cursor != null)
            cursor.close();
        return list;
    }
    //切换试卷
    public ArrayList<Library> getAllLibrary(){
        ArrayList<Library> list = new ArrayList<>();
        String sql = "select DISTINCT(name) from library";
        Cursor cursor = db.rawQuery(sql, null);
        while (cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            Library library = new Library();
            library.setLibrary_name(name);
            list.add(library);
        }
        if(cursor != null)
            cursor.close();
        return list;
    }
    //获取每个librarname的信息
    public ArrayList<Library> getLibraryInfo(ArrayList<Library> namelist){
        ArrayList<Library> list = new ArrayList<>();
        String sql0 = "select count(*) from library where name=? and flag=0";
        String sql1 = "select count(*) from library where name=? and flag=1";
        Cursor cursor = null;
        for(int i =  0;i<namelist.size();i++) {
            cursor = db.rawQuery(sql0, new String[]{namelist.get(i).getLibrary_name()});
            cursor.moveToFirst();
            String flag0 = cursor.getString(cursor.getColumnIndex("count(*)"));
            namelist.get(i).setScore(flag0);
            cursor = db.rawQuery(sql1, new String[]{namelist.get(i).getLibrary_name()});
            cursor.moveToFirst();
            String flag1 = cursor.getString(cursor.getColumnIndex("count(*)"));
            namelist.get(i).setTime(flag1);
            list.add(namelist.get(i));
        }
        if(cursor!= null)
            cursor.close();
        return list;
    }
    public String existLibrary(String libraryname){
        String sql = "select count(*) from library where name=?";
        Cursor cursor = db.rawQuery(sql, new String[]{libraryname});
        cursor.moveToFirst();
        String flag1 = cursor.getString(cursor.getColumnIndex("count(*)"));
        if(cursor != null)
            cursor.close();
        return flag1;
    }
    //删除题库
    public int deleteLibrary(String libraryname){
        String sql = "select DISTINCT(num) from library where name=?";
        Cursor cursor = db.rawQuery(sql, new String[]{libraryname});
        int flag1 = -1;
        if(cursor.getCount() == 0){
            flag1 = -1;
        }else {
            cursor.moveToFirst();
            flag1 = cursor.getInt(cursor.getColumnIndex("num"));
        }
        if(cursor != null)
            cursor.close();
        return flag1;
    }
    public String deleteLibrary2(int num){
        String sql3 = "delete  from collect_wrong where question_id in(" +
                "select DISTINCT(question_id) from question where library_num=?)";
        String sql2 = "delete from question where library_num=?";
        String sql1 = "delete from library where num=?";
        db.execSQL(sql3,new Object[]{num});
        db.execSQL(sql2,new Object[]{num});
        db.execSQL(sql1,new Object[]{num});
        String flag1 = " ";
        String sql4 = "select name from library  order by num asc limit 1";
        Cursor cursor = db.rawQuery(sql4, null);
        if(!(cursor.getCount() == 0)){
            cursor.moveToFirst();
            flag1 = cursor.getString(cursor.getColumnIndex("name"));
        }
        if(cursor != null)
            cursor.close();
        return flag1;
    }
    public String getPercent(String libraryname){
        String tmp = "";
        String sql1 = "select count(*) from library where name=? and flag = 1";
        String sql2 = "select count(*) from library where name=?";
        Cursor cursor = db.rawQuery(sql2, new String[]{libraryname});
        if(!(cursor.getCount() == 0)){
            cursor.moveToFirst();
            tmp = cursor.getString(cursor.getColumnIndex("count(*)"));
            cursor = db.rawQuery(sql1, new String[]{libraryname});
            if(!(cursor.getCount() == 0)){
                cursor.moveToFirst();
                String tmp1 = cursor.getString(cursor.getColumnIndex("count(*)"));
                tmp = tmp1+"/"+tmp;
            }
        }
        if(cursor != null)
            cursor.close();
        return tmp;
    }
    public String[] getLibraryInfo(String libraryname){
        String sqlsinglecount;
        String sqlmultiplecount;
        String sqljudgecount;
        int singlecount;
        int multiplecount;
        int judgecount;
        int collectcount;
        int wrongcount;
        String sqllibraryname = "select num from library where name=?";
        Cursor cursor = db.rawQuery(sqllibraryname, new String[]{libraryname});
        cursor.moveToFirst();
        int librarynum = cursor.getInt(cursor.getColumnIndex("num"));
        if(librarynum == 0){
            return null;
        }else {
            sqlsinglecount = "select count(*) from collect_wrong where type=1 and question_id in " +
                    "(select question_id from question where library_num=?)";
            sqlmultiplecount = "select count(*) from collect_wrong where type=2 and question_id in " +
                    "(select question_id from question where library_num=?)";
            sqljudgecount = "select count(*) from collect_wrong where type=3 and question_id in " +
                    "(select question_id from question where library_num=?)";
            String sqlcollectcount = "select count(*) from collect_wrong where collect_flag=1 and question_id in " +
                    "(select question_id from question where library_num=?)";
            String sqlwrongcount = "select count(*) from collect_wrong where wrong_flag=1 and question_id in " +
                    "(select question_id from question where library_num=?)";
            cursor = db.rawQuery(sqlsinglecount, new String[]{librarynum+""});
            cursor.moveToFirst();
            singlecount = cursor.getInt(cursor.getColumnIndex("count(*)"));
            cursor = db.rawQuery(sqlmultiplecount, new String[]{librarynum+""});
            cursor.moveToFirst();
            multiplecount = cursor.getInt(cursor.getColumnIndex("count(*)"));
            cursor = db.rawQuery(sqljudgecount, new String[]{librarynum+""});
            cursor.moveToFirst();
            judgecount = cursor.getInt(cursor.getColumnIndex("count(*)"));
            cursor = db.rawQuery(sqlcollectcount, new String[]{librarynum+""});
            cursor.moveToFirst();
            collectcount = cursor.getInt(cursor.getColumnIndex("count(*)"));
            cursor = db.rawQuery(sqlwrongcount, new String[]{librarynum+""});
            cursor.moveToFirst();
            wrongcount = cursor.getInt(cursor.getColumnIndex("count(*)"));
            if(cursor != null)
                cursor.close();
            return new String[]{"      单选题数目："+singlecount,"      多选题数目："+multiplecount,"      判断题数目："+judgecount,
                    "      已收藏："+collectcount,"      目前错题："+wrongcount,};
        }
    }
}
