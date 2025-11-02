#include <sys/resource.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>

int main(int argc,char*argv[]){
  if(argc<4){fprintf(stderr,"usage: helper memMb cpuSec cmd...\\n");return 1;}
  int memMb=atoi(argv[1]),cpuSec=atoi(argv[2]);
  struct rlimit rl;
  rl.rlim_cur=rl.rlim_max=memMb*1024*1024;
  setrlimit(RLIMIT_AS,&rl);
  rl.rlim_cur=rl.rlim_max=cpuSec;
  setrlimit(RLIMIT_CPU,&rl);
  execvp(argv[3],&argv[3]);
  perror("execvp"); return 1;
}
