# ADR 001: Stop app to stop sending

## Context
It is important to be able to prevent the sending of files for printing whilst in production without impacting services attempting to send letters for printing and posting.

On the face of it, this sounds like a feature switch, and this is likely to be the case strategically. Given the short timescales for go live for this product, this may not be achievable.

## Decision
To fulfil this requirement, the send-letter-consumer-service will simply be disabled from Azure Portal.

## Status 
Accepted

## Consequences
This means involving those with higher permissions withing Azure Portal in order to disable an application. This shouldn't be an issues as these are typically the people involved with the day to day operations of the systems.
