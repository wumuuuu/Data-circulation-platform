package com.example.demo.Model;

import java.util.List;

public class CreateTaskRequest {
    private Signer signer;
    private String selectFile;
    private String taskType;

    // Getter 和 Setter

    public Signer getSigner() {
        return signer;
    }

    public void setSigner(Signer signer) {
        this.signer = signer;
    }

    public String getSelectFile() {
        return selectFile;
    }

    public void setSelectFile(String selectFile) {
        this.selectFile = selectFile;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public static class Signer {
        private List<SignerMember> members;

        public List<SignerMember> getMembers() {
            return members;
        }

        public void setMembers(List<SignerMember> members) {
            this.members = members;
        }
    }

    public static class SignerMember {
        private String username;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
}
