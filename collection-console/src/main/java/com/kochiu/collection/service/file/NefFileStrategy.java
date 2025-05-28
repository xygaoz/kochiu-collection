package com.kochiu.collection.service.file;

import com.kochiu.collection.annotation.FileType;
import com.kochiu.collection.enums.ResourceTypeEnum;
import org.springframework.stereotype.Service;

import static org.bytedeco.libraw.global.LibRaw.libraw_version;

@Service("nef")
@FileType(thumb = true, resolutionRatio = true, mimeType = "image/x-nikon-nef", desc = ResourceTypeEnum.IMAGE)
public class NefFileStrategy extends DngFileStrategy{

    static {
        System.out.println("LibRaw version: " + libraw_version());
    }
}
