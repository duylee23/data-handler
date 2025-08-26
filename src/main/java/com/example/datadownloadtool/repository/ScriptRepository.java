package com.example.datadownloadtool.repository;

import com.example.datadownloadtool.model.entity.Script;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScriptRepository extends JpaRepository<Script, Long> {

}
