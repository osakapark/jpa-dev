package com.mystudy.study;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.mystudy.domain.Study;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {

	boolean existsByPath(String path);

	@EntityGraph(value = "Study.withAll", type = EntityGraph.EntityGraphType.LOAD)
	Study findByPath(String path);

	@EntityGraph(value = "Study.withTagsAndManagers", type = EntityGraph.EntityGraphType.FETCH)
	Study findMemberWithTagsByPath(String path);

	@EntityGraph(value = "Study.withZonesAndManagers", type = EntityGraph.EntityGraphType.FETCH)
	Study findMemberWithZonesByPath(String path);
}
