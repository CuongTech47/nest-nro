package com.ngocrong.ngocrong.network;

import com.ngocrong.ngocrong.config.AppConfig;
import com.ngocrong.ngocrong.consts.Cmd;
import com.ngocrong.ngocrong.server.DragonBall;
import com.ngocrong.ngocrong.server.Server;
import com.ngocrong.ngocrong.user.Char;
import com.ngocrong.ngocrong.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

public class Service implements IService{
    private static final Logger logger = LoggerFactory.getLogger(Service.class);
    public static int[][] PET = { { 281, 361, 351 }, { 512, 513, 536 }, { 514, 515, 537 } };
    private Session session;
    private Char player;
    private byte[] small, bg;

    public Service(Session session) {
        this.session = session;
    }
    @Override
    public void setChar(Char _char) {
        this.player = _char;
    }

    @Override
    public void close() {
        small = null;
        bg = null;
        session = null;
        player = null;
    }

    public Service(Char player) {
        this.player = player;
    }

    @Override
    public void setResource() {
        Server server = DragonBall.getInstance().getServer();
        try {
            small = server.smallVersion[session.zoomLevel - 1];
            bg = server.backgroundVersion[session.zoomLevel - 1];
        } catch (NullPointerException ex) {
            logger.error("set resource err: " + ex.getMessage(), ex);
        }
    }


    public void dialogMessage(String text) {
        try {
            Message ms = new Message(Cmd.DIALOG_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(text);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            logger.error("failed!", ex);
        }
    }



    private void sendMessage(Message ms) {
        if (player != null && !player.isHuman()) {
            return;
        }
        if (session != null) {
            this.session.sendMessage(ms);
        }
    }

    public void size(int size) {
        try {
            Message mss = new Message(Cmd.GET_IMAGE_SOURCE);
            DataOutputStream ds = mss.writer();
            ds.writeByte(1);
            ds.writeShort(size);
            ds.flush();
            sendMessage(mss);
            mss.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void downloadOk() {
        try {
            Server server = DragonBall.getInstance().getServer();
            Message mss = new Message(Cmd.GET_IMAGE_SOURCE);
            DataOutputStream ds = mss.writer();
            ds.writeByte(3);
            ds.writeInt(server.resVersion[session.zoomLevel - 1]);
            ds.flush();
            sendMessage(mss);
            mss.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }


    public void changeBodyMob(Mob mob, byte type) {
        try {
            Message msg = new Message(Cmd.CHAGE_MOD_BODY);
            DataOutputStream ds = msg.writer();
            ds.writeByte(type);
            ds.writeInt(mob.mobId);
            if (type == 1) {
                ds.writeShort(mob.body);
            }
            ds.flush();
            sendMessage(msg);
            msg.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void mapTransport(List<KeyValue> list) {
        try {
            Message msg = new Message(Cmd.MAP_TRASPORT);
            DataOutputStream ds = msg.writer();
            ds.writeByte(list.size());
            for (KeyValue<Integer, String> keyValue : list) {
                ds.writeUTF(keyValue.value);
                ds.writeUTF((String) keyValue.elements[0]);
            }
            ds.flush();
            sendMessage(msg);
            msg.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void focus(int charID) {
        try {
            Message msg = new Message(Cmd.ME_CUU_SAT);
            DataOutputStream ds = msg.writer();
            ds.writeInt(charID);
            ds.flush();
            sendMessage(msg);
            msg.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void messageTime(MessageTime message) {
        try {
            Message msg = new Message(Cmd.MESSAGE_TIME);
            DataOutputStream ds = msg.writer();
            ds.writeByte(message.getId());
            ds.writeUTF(message.getText());
            ds.writeShort(message.getTime());
            ds.flush();
            sendMessage(msg);
            msg.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void specialSkill(byte type) {
        try {
            Message msg = new Message(Cmd.SPEACIAL_SKILL);
            DataOutputStream ds = msg.writer();
            ds.writeByte(type);
            if (type == 0) {
                SpecialSkill skill = player.getSpecialSkill();
                if (skill != null) {
                    ds.writeShort(skill.getIcon());
                    ds.writeUTF(skill.getInfo2());
                } else {
                    ds.writeShort(5223);
                    ds.writeUTF(Language.NO_SPECIAL_SKILLS_YET);
                }
            }
            if (type == 1) {
                List<SpecialSkillTemplate> list = SpecialSkill.getListSpecialSkill(player.gender);
                ds.writeByte(1);
                ds.writeUTF(Language.MENU_SPECIAL_SKILL_NAME);
                ds.writeByte(list.size());
                for (SpecialSkillTemplate sp : list) {
                    ds.writeShort(sp.getIcon());
                    ds.writeUTF(sp.getInfo());
                }
            }
            ds.flush();
            sendMessage(msg);
            msg.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void fusion(Char _char, byte type) {
        try {
            Message msg = new Message(Cmd.FUSION);
            DataOutputStream ds = msg.writer();
            ds.writeByte(type);
            ds.writeInt(_char.id);
            ds.flush();
            sendMessage(msg);
            msg.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void resetPoint() {
        try {
            Message msg = new Message(Cmd.RESET_POINT);
            DataOutputStream ds = msg.writer();
            ds.writeShort(player.getX());
            ds.writeShort(player.getY());
            ds.flush();
            sendMessage(msg);
            msg.cleanup();
        } catch (IOException ex) {
            logger.error("failed!", ex);
        }
    }

    public void setLinkListServer() {
        try {
            Server server = DragonBall.getInstance().getServer();
            AppConfig config = server.getAppConfig();
            Message ms = messageNotLogin(Cmd.CLIENT_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(config.getListServers());
            ds.writeByte(0);
            ds.flush();
            sendMessage(ms);
            ms.cleanup();
        } catch (Exception ex) {
            logger.error("failed!", ex);
        }
    }

    public static Message messageNotLogin(int command) {
        try {
            Message ms = new Message(Cmd.NOT_LOGIN);
            ms.writer().writeByte(command);
            return ms;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void download(String path) {
        try {
            String str = path.replace("\\", "/").replace("resources/data/" + session.zoomLevel, "");
            str = Utils.cutPng(str);
            Message mss = new Message(Cmd.GET_IMAGE_SOURCE);
            DataOutputStream ds = mss.writer();
            ds.writeByte(2);
            ds.writeUTF(str);
            byte[] ab = Utils.getFile(path);
            ds.writeInt(ab.length);
            ds.write(ab);
            ds.flush();
            sendMessage(mss);
            mss.cleanup();
        } catch (IOException ex) {
            logger.error("download error", ex);
        }
    }
}
