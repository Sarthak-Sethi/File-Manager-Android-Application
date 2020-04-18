package com.example.filemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

import static android.text.TextUtils.concat;

public class MainActivity extends AppCompatActivity {
    Button deletebtn,newfolder,refreshbtn,backbtn,renamebtn,copybtn,pastebtn;
    ListView listView;
    private static final int PREQUESTPERMISSIONS = 1234;
    private File dir;
    String rootpath,currentpath,copypath;
    TextView pathoutput;
    private static final String[]  PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int PERMISSIONCOUNT = 2;
    private boolean filemanagerinitialized = false;
    private boolean[] selection ;
    private File[] file;
    private  boolean islongclick;
    private int selectedindex,filesffoundcount;
    ArrayList<String> fileslist;
    @Override
    public void onBackPressed() {
//        if(pathoutput.getText().equals(rootpath));
//            super.onBackPressed();
        // we want ki 0 pe aane ke baad back press ho to aap bnnd ho jaaye
        currentpath =  currentpath.substring(0,currentpath.lastIndexOf('/'));
        refreshbtn.callOnClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view);
         pathoutput = findViewById(R.id.pathoutput);
        deletebtn = findViewById(R.id.deletebtn);
        renamebtn = findViewById(R.id.rename);
        copybtn = findViewById(R.id.copybtn);
        pastebtn = findViewById(R.id.pastebtn);
        newfolder = findViewById(R.id.newfolderbtn);
        refreshbtn = findViewById(R.id.refresh);
      //  backbtn = findViewById(R.id.backbtn);
    }
    private boolean ispermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            int perptr = 0;
            while(perptr<PERMISSIONCOUNT) {
                if (checkSelfPermission(PERMISSIONS[perptr]) == PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
                perptr++;
            }
        }return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!ispermission()){
            requestPermissions(PERMISSIONS,PREQUESTPERMISSIONS);
            return;
        }
        if(!filemanagerinitialized){
             currentpath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
             rootpath = currentpath.substring(0,currentpath.lastIndexOf('/'));
            pathoutput.setText(currentpath.substring(currentpath.lastIndexOf('/')+1));

            listView = findViewById(R.id.listview);
            final Adapter adapter = new Adapter();
            listView.setAdapter(adapter);

            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    islongclick = true;
                    selection[position] =! selection[position];
                    adapter.setSelection(selection);
                    //boolean leastoneselected = false;
                    int selectioncount=0;
                    for (boolean b : selection) {
                        if (b) {
                            //  leastoneselected = true;
                            selectioncount++;
                        }
                    }
                    if (selectioncount>0) {
                        if(selectioncount==1){
                            findViewById(R.id.rename).setVisibility(View.VISIBLE);
                            selectedindex = position;
                        }else{
                            findViewById(R.id.rename).setVisibility(View.GONE);
                        }
                        findViewById(R.id.btnbar).setVisibility(View.VISIBLE);
                    } else {
                        findViewById(R.id.btnbar).setVisibility(View.GONE);
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            islongclick = false;
                        }
                    },1000);
                    return false;
                }
            });
            deletebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder deletedialog = new AlertDialog.Builder(MainActivity.this);
                    deletedialog.setTitle("DELETE");
                    deletedialog.setMessage("KRDIYE DEL... MUK GYE PAPER???");
                    deletedialog.setPositiveButton("AAHO !!!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for(int i=0;i<file.length;i++){
                                if(selection[i]){
                                    deleteFile(file[i]);
                                    selection[i]  = false;
                                }
                            }
                            refreshbtn.callOnClick();
                        }
                    });
                    deletedialog.setNegativeButton("HALE KITHE ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                        }
                    });
                    deletedialog.show();
                   // refreshbtn.callOnClick();
                    //selection = new boolean[file.length];
                    //Adapter adapter= new Adapter();
                  //  adapter.setSelection(selection);
                }
            });

            newfolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder newfolderdialog = new AlertDialog.Builder(MainActivity.this);
                    newfolderdialog.setTitle("New Folder");
                    final EditText input = new EditText(MainActivity.this);
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    newfolderdialog.setView(input);
                    newfolderdialog.setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final File  newfolder = new File(currentpath+"/"+input.getText());
                            if(!newfolder.exists()){
                                newfolder.mkdir();
                                Toast.makeText(MainActivity.this, "Folder created successfull", Toast.LENGTH_SHORT).show();
                                refreshbtn.callOnClick();
                            }
                        }
                    });
                    newfolderdialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                        }
                    });
                newfolderdialog.show();
                }
            });
            refreshbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //dir = new File(currentpath);
                    pathoutput.setText(currentpath.substring(currentpath.lastIndexOf('/')+1));
                    dir = new File(currentpath);
                    file = dir.listFiles();
                    filesffoundcount = file.length;
                    selection = new boolean[filesffoundcount];
                    adapter.setSelection(selection);
                    fileslist = new ArrayList<>();
                    for(int i=0;i<filesffoundcount;i++){
                        fileslist.add(String.valueOf(file[i].getAbsolutePath()));
                    }
                    adapter.setData(fileslist);
//                    file = dir.listFiles();
//                    if(file == null)
//                        return ;
//                    fileslist.clear();
//                    for(int i=0;i<file.length;i++){
//                        fileslist.add(String.valueOf(file[i].getAbsolutePath()));
//                    }
//
                }
            });
            renamebtn.callOnClick();

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!islongclick) {
                                if (position > file.length) {
                                    return ;
                                }
                                if (file[position].isDirectory()) {
                                    currentpath = file[position].getAbsolutePath();
                                    dir = new File(currentpath);
                                    pathoutput.setText(currentpath.substring(currentpath.lastIndexOf('/') + 1));
                                    refreshbtn.callOnClick();
                                }
                            }
                        }
                    },50);



                }
            });
            renamebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder renamedialog = new AlertDialog.Builder(MainActivity.this);
                    renamedialog.setTitle("Rename to");
                    final EditText renameetxt = new EditText(MainActivity.this);
                    final String renamepath = file[selectedindex].getAbsolutePath();
                    renameetxt.setText(renamepath.substring(renamepath.lastIndexOf('/')));
                    renameetxt.setInputType(InputType.TYPE_CLASS_TEXT);
                    renamedialog.setView(renameetxt);
                    renamedialog.setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                       String s = new File(renamepath).getParent()+"/"+concat(renameetxt.getText());
                       File renamedname = new File(s);
                       new File(renamepath).renameTo(renamedname);
                       refreshbtn.callOnClick();
                       selection = new boolean[file.length];
                       adapter.setSelection(selection);
                        }
                    });
                    renamedialog.show();
                }
            });
            copybtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    copypath = file[selectedindex].getAbsolutePath();
                    selection = new boolean[file.length];
                    adapter.setSelection(selection);
                    findViewById(R.id.pastebtn).setVisibility(View.VISIBLE);

                }
            });
            pastebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pastebtn.setVisibility(View.GONE);
                    String dstpath =currentpath + copypath.substring(copypath.lastIndexOf('/'));
                    copy(new File(copypath),new File(dstpath));
                    file = new File(currentpath).listFiles();
                    selection = new boolean[file.length];
                    adapter.setSelection(selection);
                    refreshbtn.callOnClick();
                }
            });
            filemanagerinitialized = true;
        }
        else{
            refreshbtn.callOnClick();
        }
    }
    private void copy(File src, File dst){
        try {
            InputStream inputStream = new FileInputStream(src);
            OutputStream outputStream = new FileOutputStream(dst);
            byte[] buffer = new byte[1024];
            int length;
            while((length = inputStream.read(buffer))>0){
                outputStream.write(buffer,0,length);
            }
            outputStream.close();
            inputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void deleteFile(File file) {
        if(file.isDirectory()){
            if(file.list().length == 0){
                file.delete();
            }
            else{
                String files[] = file.list();
                for(String string : files){
                    File deletefile = new File(file,string);
                    deleteFile(deletefile);
                }
                if(file.list().length == 0){
                    file.delete();
                }
            }
        }
        else{
            file.delete();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PREQUESTPERMISSIONS && grantResults.length>0){
            if(!ispermission()){
                ((ActivityManager) Objects.requireNonNull(this.getSystemService(ACTIVITY_SERVICE))).clearApplicationUserData();
                recreate();
            }else{
                onResume();
            }
        }
    }
}

