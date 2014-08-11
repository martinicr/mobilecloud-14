package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VideoController {

    private static final AtomicLong currentId = new AtomicLong(0L);

    @Autowired
    private VideoFileManager videoManager;

    @Autowired
    private VideoRepository videoRepository;

    @RequestMapping(value = "/video", method = RequestMethod.GET)
    @ResponseBody
    public Collection<Video> getVideos(){
        return videoRepository.findAll();
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST)
    @ResponseBody
    public Video addVideo(@RequestBody Video video){
        checkAndSetId(video);
        video.setDataUrl(getDataUrl(video.getId()));
        this.videoRepository.save(video);

        return video;
    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
    @ResponseBody
    public VideoStatus setVideoData(@PathVariable Long id, @RequestParam("data") MultipartFile data) throws Exception{
        Video v = checkVideoId(id);
        this.videoManager.saveVideoData(v, data.getInputStream());

        return new VideoStatus(VideoStatus.VideoState.READY);
    }


    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public void getData(@PathVariable Long id, HttpServletResponse response) throws Exception{
        Video v = checkVideoId(id);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        if(this.videoManager.hasVideoData(v)){
            ServletOutputStream out = response.getOutputStream();
            FileSystemResource r = new FileSystemResource(v.getLocation());
            InputStream in = r.getInputStream();
            byte[] buffer = new byte[2048];
            int bytesRead;
            while((bytesRead = in.read(buffer)) != -1){
                  out.write(buffer, 0, bytesRead);
            }
        }

    }

    @ExceptionHandler(VideoNotFoundException.class)
    public ResponseEntity<String> videoNotFoundExceptionHandler(VideoNotFoundException e){
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> fileHandlingExceptionHandler(IOException e){
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }

    private Video checkVideoId(Long id) throws VideoNotFoundException {
        Video v = videoRepository.findVideo(id);
        if(null == v){
            throw new VideoNotFoundException("Video ID = " + id +" not found");
        }
        return v;
    }

    private String getDataUrl(long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String base =
                "http://"+request.getServerName()
                        + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
        return base;
    }

}
