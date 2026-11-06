#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <string.h>
#include <stdint.h>
#include <stdio.h>
#include <stdarg.h>

struct drm_version_local {
 int version_major,version_minor,version_patchlevel;
 unsigned long name_len; char *name;
 unsigned long date_len; char *date;
 unsigned long desc_len; char *desc;
};
struct drm_get_cap_local { uint64_t capability; uint64_t value; };
struct drm_mode_card_res_local {
 uint64_t fb_id_ptr, crtc_id_ptr, connector_id_ptr, encoder_id_ptr;
 uint32_t count_fbs,count_crtcs,count_connectors,count_encoders;
 uint32_t min_width,max_width,min_height,max_height;
};
#define DRM_VERSION_REQ _IOWR('d',0x00,struct drm_version_local)
#define DRM_GET_CAP_REQ _IOWR('d',0x0c,struct drm_get_cap_local)
#define DRM_GET_RES_REQ _IOWR('d',0xa0,struct drm_mode_card_res_local)
static char output[12000]; static size_t used;
static void line(const char *fmt,...) {
 if(used>=sizeof(output)-2)return;
 va_list args;va_start(args,fmt);
 int n=vsnprintf(output+used,sizeof(output)-used,fmt,args);va_end(args);
 if(n>0)used+=(size_t)n<sizeof(output)-used?(size_t)n:sizeof(output)-used-1;
}
static void probe(const char *path) {
 line("\n%s\n",path);
 int fd=open(path,O_RDONLY|O_CLOEXEC|O_NONBLOCK);
 if(fd<0){line("open: FAILED errno=%d (%s)\n",errno,strerror(errno));return;}
 line("open: SUCCESS\n");
 char name[256]={0},date[128]={0},desc[512]={0};
 struct drm_version_local v={0};
 v.name_len=sizeof(name)-1;v.name=name;
 v.date_len=sizeof(date)-1;v.date=date;
 v.desc_len=sizeof(desc)-1;v.desc=desc;
 if(ioctl(fd,DRM_VERSION_REQ,&v)==0)
  line("DRM_IOCTL_VERSION: SUCCESS\ndriver=%s version=%d.%d.%d date=%s\ndescription=%s\n",name,v.version_major,v.version_minor,v.version_patchlevel,date,desc);
 else line("DRM_IOCTL_VERSION: FAILED errno=%d (%s)\n",errno,strerror(errno));
 const uint64_t caps[]={1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
 for(int i=0;i<16;i++){
  struct drm_get_cap_local cap={caps[i],0};
  if(ioctl(fd,DRM_GET_CAP_REQ,&cap)==0)
   line("GET_CAP %llu: SUCCESS value=%llu\n",(unsigned long long)caps[i],(unsigned long long)cap.value);
  else line("GET_CAP %llu: FAILED errno=%d (%s)\n",(unsigned long long)caps[i],errno,strerror(errno));
 }
 struct drm_mode_card_res_local r={0};
 if(ioctl(fd,DRM_GET_RES_REQ,&r)==0)
  line("MODE_GETRESOURCES: SUCCESS crtc=%u connectors=%u encoders=%u fb=%u\n",r.count_crtcs,r.count_connectors,r.count_encoders,r.count_fbs);
 else line("MODE_GETRESOURCES: FAILED errno=%d (%s)\n",errno,strerror(errno));
 close(fd);
}
JNIEXPORT jstring JNICALL Java_com_lightlab_s22_MainActivity_nativeProbe(JNIEnv *env,jclass clazz){
 used=0;output[0]=0;
 line("Native read-only DRM ioctl probe. No display writes.\n");
 probe("/dev/dri/card0");probe("/dev/dri/card1");
 probe("/dev/dri/renderD128");probe("/dev/dri/renderD129");
 line("\nThese DRM operations do not measure OLED photocurrent.\n");
 return (*env)->NewStringUTF(env,output);
}
