package cn.smartjavaai.face.dao;

import cn.smartjavaai.face.entity.FaceData;
import cn.smartjavaai.face.sqllite.RowMapper;
import cn.smartjavaai.face.sqllite.SqliteHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 人脸库持久层
 * @author dwj
 */
@Slf4j
public class FaceDao {

    private static final String TABLE_NAME_IMG = "face";


    private String dbFilePath;

    public FaceDao(String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }


    public void save(FaceData faceData) throws SQLException, ClassNotFoundException {
        SqliteHelper sqliteHelper = new SqliteHelper(dbFilePath);
        sqliteHelper.executeUpdate("INSERT OR REPLACE INTO " + TABLE_NAME_IMG + " (\"index\",\"key\",\"img_data\",\"width\",\"height\",\"channel\") VALUES (?,?,?,?,?,?)", new Object[]{faceData.getIndex(),faceData
                .getKey(), faceData.getImgData(), faceData.getWidth(), faceData.getHeight(), faceData.getChannel()});
    }

    /**
     * 使用index查询key
     *
     * @param index
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public String findKeyByIndex(int index) throws SQLException, ClassNotFoundException {
        SqliteHelper sqliteHelper = new SqliteHelper(dbFilePath);
        return sqliteHelper.executeQuery("select \"key\" from " + TABLE_NAME_IMG + " where \"index\"=" + index);
    }

    /**
     * 删除全部
     *
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public long deleteAll() throws SQLException, ClassNotFoundException {
        SqliteHelper sqliteHelper = new SqliteHelper(dbFilePath);
        long rows = sqliteHelper.executeUpdate("delete from " + TABLE_NAME_IMG);
        return rows;
    }

    /**
     * 查询index
     *
     * @param keys
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public List<Long> findIndexList(String... keys) throws SQLException, ClassNotFoundException {
        // 使用 Stream API
        String inKeys = Arrays.stream(keys)
                .map(s -> "'" + s + "'")
                .reduce((s1, s2) -> s1 + "," + s2)
                .orElse("");
        String sql = "select \"index\" from " + TABLE_NAME_IMG + " where \"key\" in (" + inKeys + ")";
        log.info("sql：{}", sql.toString());
        SqliteHelper sqliteHelper = new SqliteHelper(dbFilePath);
        return sqliteHelper.executeQuery(sql, new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int index) throws SQLException {
                return rs.getLong(1);
            }
        });
    }

    /**
     * 删除人脸
     * @param keys
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public boolean deleteFace(String... keys) throws SQLException, ClassNotFoundException {
        String inKeys = Arrays.stream(keys)
                .map(s -> "'" + s + "'")
                .reduce((s1, s2) -> s1 + "," + s2)
                .orElse("");

        SqliteHelper sqliteHelper = new SqliteHelper(dbFilePath);
        String sql = "delete from " + TABLE_NAME_IMG + " where \"key\" in (" + inKeys + ")";
        log.info("sql：{}", sql.toString());
        sqliteHelper.executeUpdate(sql);
        return true;
    }

    /**
     * 分页查询人脸
     * @param pageNo
     * @param pageSize
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public List<FaceData> findFace(int pageNo, int pageSize) throws SQLException, ClassNotFoundException {
        String sql = "select \"key\",\"img_data\",\"width\",\"height\",\"channel\" from " + TABLE_NAME_IMG +
                " limit " + pageNo * pageSize + "," + pageSize;
        SqliteHelper sqliteHelper = new SqliteHelper(dbFilePath);
        return sqliteHelper.executeQuery(sql, new RowMapper<FaceData>() {
            @Override
            public FaceData mapRow(ResultSet rs, int index) throws SQLException {
                FaceData face = new FaceData();
                face.setKey(rs.getString("key"));
                face.setImgData(rs.getBytes("img_data"));
                face.setWidth(rs.getInt("width"));
                face.setHeight(rs.getInt("height"));
                face.setChannel(rs.getInt("channel"));
                return face;
            }
        });
    }

    /**
     * 更新index
     * @param index
     * @param faceData
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int updateIndex(long index,FaceData faceData) throws SQLException, ClassNotFoundException {
        SqliteHelper sqliteHelper = new SqliteHelper(dbFilePath);
        return sqliteHelper.executeUpdate("INSERT OR REPLACE INTO " + TABLE_NAME_IMG + " (\"index\",\"key\",\"img_data\",\"width\",\"height\",\"channel\") VALUES (?,?,?,?,?,?)", new Object[]{index,faceData
                .getKey(), faceData.getImgData(), faceData.getWidth(), faceData.getHeight(), faceData.getChannel()});
    }

}
