package icu.mmmc.xchat;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import icu.mmmc.xchat.adapters.ServerItemAdapter;
import icu.mmmc.xchat.utils.DataBase;
import icu.xchat.core.XChatCore;
import icu.xchat.core.callbacks.adapters.ProgressAdapter;
import icu.xchat.core.entities.ServerInfo;
import icu.xchat.core.utils.ServerInfoUtils;

public class ServerManagerActivity extends AppCompatActivity {
    private DataBase dataBase;
    private ListView serverList;
    private ServerItemAdapter serverItemAdapter;
    private ServerInfo currentServerInfo;

    private void loadViews() {
        findViewById(R.id.backBtn).setOnClickListener(e -> finish());
        findViewById(R.id.add_btn).setOnClickListener(e -> {
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.server_add_dialog, null);
            EditText serverTitle = layout.findViewById(R.id.server_title);
            EditText serverCode = layout.findViewById(R.id.server_code);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("添加服务器")
                    .setView(layout)
                    .setNegativeButton("取消", null)
                    .setPositiveButton("确定", (dialogInterface, i) -> {
                        try {
                            ServerInfo serverInfo = ServerInfoUtils.fromCode(serverCode.getText().toString());
                            dataBase.saveServer(serverTitle.getText().toString(), serverInfo);
                            serverItemAdapter.addItem(new ServerItemAdapter.ServerEntity(serverTitle.getText().toString(), serverInfo));
                        } catch (Exception exception) {
                            Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            builder.show();
        });
        this.serverItemAdapter = new ServerItemAdapter(this);
        this.serverList = findViewById(R.id.server_list);
        this.serverList.setAdapter(serverItemAdapter);
        this.serverList.setOnItemClickListener((adapterView, view, i, l) -> {
            ServerItemAdapter.ServerEntity serverEntity = (ServerItemAdapter.ServerEntity) serverItemAdapter.getItem(i);
            this.currentServerInfo = serverEntity.serverInfo;
            openContextMenu(serverList);
        });
    }

    @SuppressLint({"Range", "Recycle"})
    private void refresh() {
        Cursor cursor = dataBase.getReadableDatabase().rawQuery("SELECT * FROM t_servers", null);
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex("server_title"));
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServerCode(cursor.getString(cursor.getColumnIndex("server_code")));
            serverInfo.setHost(cursor.getString(cursor.getColumnIndex("host")));
            serverInfo.setPort(cursor.getInt(cursor.getColumnIndex("port")));
            ServerItemAdapter.ServerEntity serverEntity = new ServerItemAdapter.ServerEntity(title, serverInfo);
            serverItemAdapter.addItem(serverEntity);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.server_menu, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (Objects.equals(item.getItemId(), R.id.connect)) {
            if (XChatCore.getIdentity() == null) {
                Toast.makeText(ServerManagerActivity.this, "请先加载身份！", Toast.LENGTH_SHORT).show();
                return false;
            }
            ServerInfo info = currentServerInfo;
            XChatCore.Servers.attemptConnectServer(info, new ProgressAdapter() {

                @Override
                public void startProgress() {
                    runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, "开始连接！", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void completeProgress() {
                    runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, "连接成功！", Toast.LENGTH_SHORT).show());
                    XChatCore.Tasks.syncIdentity(info.getServerCode(), new ProgressAdapter() {
                        @Override
                        public void completeProgress() {
                            runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, "身份同步完毕！", Toast.LENGTH_SHORT).show());
                            XChatCore.Tasks.syncRoom(info.getServerCode(), new ProgressAdapter() {
                                @Override
                                public void completeProgress() {
                                    runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, "房间同步完毕！", Toast.LENGTH_SHORT).show());
                                }

                                @Override
                                public void terminate(String errMsg) {
                                    runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, errMsg, Toast.LENGTH_SHORT).show());
                                }
                            });
                            XChatCore.Tasks.syncUser(info.getServerCode(), new ProgressAdapter() {
                                @Override
                                public void completeProgress() {
                                    runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, "用户同步完毕！", Toast.LENGTH_SHORT).show());
                                }

                                @Override
                                public void terminate(String errMsg) {
                                    runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, errMsg, Toast.LENGTH_SHORT).show());
                                }
                            });
                        }

                        @Override
                        public void terminate(String errMsg) {
                            runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, errMsg, Toast.LENGTH_SHORT).show());
                        }
                    });
                }

                @Override
                public void terminate(String errMsg) {
                    runOnUiThread(() -> Toast.makeText(ServerManagerActivity.this, errMsg, Toast.LENGTH_SHORT).show());
                }
            });
        } else if (Objects.equals(item.getItemId(), R.id.details)) {
            LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.server_item_details, null);
            TextView serverId = layout.findViewById(R.id.server_id);
            serverId.setText("服务器识别码：" + currentServerInfo.getServerCode());
            TextView serverHost = layout.findViewById(R.id.server_host);
            serverHost.setText("服务器地址：" + currentServerInfo.getHost());
            TextView serverPort = layout.findViewById(R.id.server_port);
            serverPort.setText("服务器端口：" + currentServerInfo.getPort());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("服务器详情")
                    .setView(layout)
                    .setPositiveButton("确定", null)
                    .show();
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_manager);
        dataBase = new DataBase(this);
        loadViews();
        registerForContextMenu(serverList);
        refresh();
        XChatCore.CallBack.updateOnlineServerListCallback = list -> runOnUiThread(() -> serverItemAdapter.notifyDataSetChanged());
    }
}