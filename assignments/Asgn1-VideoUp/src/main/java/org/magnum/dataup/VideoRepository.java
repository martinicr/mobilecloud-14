package org.magnum.dataup;


import org.magnum.dataup.model.Video;

import java.util.Collection;

public interface VideoRepository {

    void save(Video v);

    Video findVideo(Long id);

    Collection<Video> findAll();

}
