package com.nowcoder.community.dao.elasticsearch;

import com.nowcoder.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author qhhu
 * @date 2019/11/17 - 13:27
 */
@Repository
// 父接口elasticsearchRepository已经事先定义好对es服务器访问的增删改查各种方法, spring会自动实现
//                                                               接口处理实体的类型, 主键的类型
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {

}
