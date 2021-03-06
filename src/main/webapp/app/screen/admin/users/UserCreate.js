import React from 'react';

import {BooleanInput, Create, NumberInput, SimpleForm, TextInput} from "react-admin";
import AvatarField from "../../../core/field/AvatarField"; // eslint-disable-line import/no-unresolved


const UserCreate = ({permissions, ...props}) => (
    <Create {...props}>
        <SimpleForm>
            <TextInput source="id" label="resources.users.fields.id"/>
            <TextInput source="name" label="resources.users.fields.name"/>
            <NumberInput source="version" label="resources.users.fields.version"/>
            <AvatarField size={40} source="imageUrl" label="resources.users.fields.imageUrl"/>
            <TextInput source="email" label="resources.users.fields.email"/>
            <BooleanInput source="emailVerified" label="resources.users.fields.emailVerified"/>
            <TextInput source="provider" label="resources.users.fields.provider"/>
            <TextInput source="providerId" label="resources.users.fields.providerId"/>
        </SimpleForm>
    </Create>
);

export default UserCreate;
