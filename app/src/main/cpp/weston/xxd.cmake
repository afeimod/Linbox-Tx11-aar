find_package(Python3)
if (NOT Python3_FOUND)
    message(FATAL_ERROR "Python3 not found, please install it to continue.")
endif ()

function(xxd py_path input output name)
    execute_process(
            COMMAND ${Python3_EXECUTABLE} ${py_path} ${input} ${output} -n ${name}
            ERROR_VARIABLE error_out
            RESULT_VARIABLE ret
    )
    if (NOT ret EQUAL 0)
        message(FATAL_ERROR "Exec: [${Python3_EXECUTABLE} ${py_path} ${input} ${output} -n ${name}] Error: ${error_out}")
    endif ()
endfunction()
