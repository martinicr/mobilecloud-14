package org.magnum.dataup;


import org.magnum.dataup.model.Video;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class VideoRepositoryImpl implements VideoRepository{

    private Map<Long, Video> videoDataSource;

    public VideoRepositoryImpl(){
        videoDataSource = new LinkedHashMap<>();
    }

    @Override
    public void save(Video v) {
        videoDataSource.put(v.getId(), v);
    }

    @Override
    public Video findVideo(Long id) {
        return videoDataSource.get(id);
    }

    @Override
    public Collection<Video> findAll() {
        return videoDataSource.values();
    }
}
