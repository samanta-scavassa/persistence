package com.tr.persistence.dao;
import com.tr.persistence.Entities.Officer;
import com.tr.persistence.Entities.Rank;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection", "ConstantConditions"})
@Repository
public class JdbcOfficerDAO implements OfficerDAO {
    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert insertOfficer;

    public JdbcOfficerDAO(DataSource dataSource){
        jdbcTemplate = new JdbcTemplate(dataSource);
        insertOfficer = new SimpleJdbcInsert(dataSource)
                .withTableName("officers")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public Officer save(Officer officer){
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("rank", officer.getRank());
        parameters.put("first", officer.getFirst());
        parameters.put("last", officer.getLast());
        Integer newId = (Integer) insertOfficer.executeAndReturnKey(parameters);
        officer.setId(newId);
        return officer;
    }

    @Override
    public Optional<Officer> findById(Integer id){
        if(!existsById(id)) return Optional.empty();
        return Optional.of(jdbcTemplate.queryForObject(
                "SELECT * FROM officers WHERE id=?",
                new RowMapper<Officer>() {
                    @Override
                    public Officer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new Officer(
                                rs.getInt("id"),
                                Rank.valueOf(rs.getString("rank")),
                                rs.getString("first_name"),
                                rs.getString("last_name"));
                    }
                },
                id));

    }

    @Override
    public List<Officer> findAll(){
        return jdbcTemplate.query("SELECT * FROM officers",
                (rs, rowNum) -> new Officer(rs.getInt("id"),
                        Rank.valueOf(rs.getString("rank")),
                        rs.getString("first_name"),
                        rs.getString("last_name")));
    }

    @Override
    public long count(){
        return jdbcTemplate.queryForObject(
                "select count(*) from officers", Long.class);
    }

    @Override
    public void delete(Officer officer){
        jdbcTemplate.update("DELETE FROM officers where id=?", officer.getId());
    }

    @Override
    public boolean existsById(Integer id){
        return jdbcTemplate.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM officers where id=?)", Boolean.class, id);
    }

}
