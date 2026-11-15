#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <errno.h>
#include <string.h>
#include <stdint.h>
#include <stdio.h>
#include <stdarg.h>
#include <stdlib.h>
struct ver {int a,b,c;unsigned long nl;char *n;unsigned long dl;char *d;unsigned long xl;char *x;};
struct res {uint64_t fb,crtc,conn,enc;uint32_t nf,nc,nn,ne,minw,maxw,minh,maxh;};
struct connector {uint64_t enc,mode,props,values;uint32_t nm,np,ne,id,type,typeid,connection,mmw,mmh,subpixel,pad;};
struct objprops {uint64_t props,values;uint32_t count,id,type;};
struct prop {uint64_t values,enum_blob;uint32_t id,flags;char name[32];uint32_t count_values,count_enum;};
#define VERSION _IOWR('d',0x00,struct ver)
#define RES _IOWR('d',0xa0,struct res)
#define CONNECTOR _IOWR('d',0xa7,struct connector)
#define OBJPROPS _IOWR('d',0xb9,struct objprops)
#define PROPERTY _IOWR('d',0xaa,struct prop)
#define DRM_MODE_OBJECT_CONNECTOR 0xc0c0c0c0U
static char out[24000];static size_t used;
static void add(const char *fmt,...){if(used>=sizeof(out)-2)return;va_list a;va_start(a,fmt);int n=vsnprintf(out+used,sizeof(out)-used,fmt,a);va_end(a);if(n>0){size_t room=sizeof(out)-used-1;used+=(size_t)n<room?(size_t)n:room;}}
static void check(const char *path,int stage){
 int fd=open(path,O_RDONLY|O_CLOEXEC|O_NONBLOCK);
 add("\n%s: ",path);
 if(fd<0){add("OPEN FAIL errno=%d\n",errno);return;}
 add("OPEN OK\n");
 char name[128]={0};struct ver v={0};v.nl=127;v.n=name;
 if(ioctl(fd,VERSION,&v)){add("DRM_VERSION FAIL errno=%d\n",errno);close(fd);return;}
 add("DRM_VERSION OK driver=%s\n",name);
 if(stage==0){close(fd);return;}
 struct res r={0};
 if(ioctl(fd,RES,&r)){add("GETRESOURCES FAIL errno=%d\n",errno);close(fd);return;}
 add("GETRESOURCES OK connectors=%u crtcs=%u\n",r.nn,r.nc);
 if(stage==1||r.nn==0||r.nn>64){close(fd);return;}
 // Supply buffers for ALL nonzero resource counts, not just connectors.
 // The v5 EFAULT likely arose from null CRTC/encoder pointers.
 uint32_t connector_count=r.nn,crtc_count=r.nc,encoder_count=r.ne,fb_count=r.nf;
 if(crtc_count>128||encoder_count>128||fb_count>4096){add("Unsafe resource count\n");close(fd);return;}
 uint32_t *ids=calloc(connector_count?connector_count:1,sizeof(uint32_t));
 uint32_t *crtcs=calloc(crtc_count?crtc_count:1,sizeof(uint32_t));
 uint32_t *encoders=calloc(encoder_count?encoder_count:1,sizeof(uint32_t));
 uint32_t *fbs=calloc(fb_count?fb_count:1,sizeof(uint32_t));
 if(!ids||!crtcs||!encoders||!fbs){add("Allocation failed\n");free(ids);free(crtcs);free(encoders);free(fbs);close(fd);return;}
 r.conn=(uintptr_t)ids;r.crtc=(uintptr_t)crtcs;r.enc=(uintptr_t)encoders;r.fb=(uintptr_t)fbs;
 if(ioctl(fd,RES,&r)){
  add("GETRESOURCES IDs FAIL errno=%d (%s)\n",errno,strerror(errno));
  free(ids);free(crtcs);free(encoders);free(fbs);close(fd);return;
 }
 add("GETRESOURCES IDs OK connectors=%u crtcs=%u encoders=%u fbs=%u\n",r.nn,r.nc,r.ne,r.nf);
 for(uint32_t i=0;i<r.nn&&i<12;i++){
  struct connector c={0};c.id=ids[i];
  if(ioctl(fd,CONNECTOR,&c)){add("connector %u GETCONNECTOR FAIL errno=%d\n",ids[i],errno);continue;}
  add("connector %u type=%u connection=%u properties=%u\n",ids[i],c.type,c.connection,c.np);
  if(stage<3)continue;
  struct objprops op={0};op.id=ids[i];op.type=DRM_MODE_OBJECT_CONNECTOR;
  if(ioctl(fd,OBJPROPS,&op)){add(" OBJ_GETPROPERTIES FAIL errno=%d\n",errno);continue;}
  if(op.count>128){add(" too many properties\n");continue;}
  uint32_t *pids=calloc(op.count?op.count:1,sizeof(uint32_t));
  uint64_t *vals=calloc(op.count?op.count:1,sizeof(uint64_t));
  if(!pids||!vals){free(pids);free(vals);continue;}
  op.props=(uintptr_t)pids;op.values=(uintptr_t)vals;
  if(ioctl(fd,OBJPROPS,&op)){add(" property values FAIL errno=%d\n",errno);}
  else for(uint32_t j=0;j<op.count&&j<60;j++){
   struct prop p={0};p.id=pids[j];
   if(ioctl(fd,PROPERTY,&p)==0)add("  %s = %llu (flags=%u)\n",p.name,(unsigned long long)vals[j],p.flags);
   else add("  property %u read FAIL errno=%d\n",pids[j],errno);
  }
  free(pids);free(vals);
 }
 free(ids);free(crtcs);free(encoders);free(fbs);close(fd);
}
JNIEXPORT jstring JNICALL Java_com_lightlab_s22_MainActivity_nativeStage(JNIEnv *env,jclass clazz,jint stage){
 used=0;out[0]=0;add("Read-only DRM discovery stage %d\n",stage);
 check("/dev/dri/card0",stage);check("/dev/dri/card1",stage);
 add("\nNo pixel photocurrent has been measured.\n");
 return (*env)->NewStringUTF(env,out);
}
