#关闭ssl 不支持rtmps
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DNO_CRYPTO"  )
#所有源文件放入 rtmp_source 变量 file函数
aux_source_directory(. rtmp_source)

#编译静态库
add_library(
             rtmp
             STATIC
            ${rtmp_source} )